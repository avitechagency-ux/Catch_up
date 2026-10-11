import { createClient } from "npm:@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
};

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const marzpayApiKey = Deno.env.get("MARZPAY_API_KEY") || Deno.env.get("MARZPAY_API_USER") || "";
    const marzpayApiSecret = Deno.env.get("MARZPAY_API_SECRET") || "";
    const marzpayBaseUrl = (Deno.env.get("MARZPAY_BASE_URL") ?? "https://wallet.wearemarz.com/api/v1").replace(/\/$/, "");

    let txId = "";
    let marzpayTxId = "";
    let userEmail = "";
    let userPhone = "";

    if (req.method === "POST") {
      const body = await req.json();
      txId = body.transaction_id || body.tx_id || "";
      marzpayTxId = body.marzpay_tx_id || body.reference || "";
      userEmail = body.email || body.user_email || "";
      userPhone = body.phone_number || body.phoneNumber || "";
    } else {
      const url = new URL(req.url);
      txId = url.searchParams.get("transaction_id") || "";
      marzpayTxId = url.searchParams.get("marzpay_tx_id") || "";
      userEmail = url.searchParams.get("email") || "";
      userPhone = url.searchParams.get("phone_number") || "";
    }

    if (!txId && !marzpayTxId) {
      return new Response(
        JSON.stringify({ success: false, error: "transaction_id or marzpay_tx_id is required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Look up transaction in payments_transactions
    let query = supabase.from("payments_transactions").select("*");
    if (txId) {
      query = query.eq("id", txId);
    } else {
      query = query.eq("reference_code", marzpayTxId);
    }
    const { data: txRecords, error: fetchError } = await query;

    if (fetchError || !txRecords || txRecords.length === 0) {
      console.warn("Transaction not found in database:", txId || marzpayTxId);
    }

    const currentTx = txRecords?.[0];
    let finalStatus = (currentTx?.status || "PENDING").toUpperCase();

    // Query MarzPay API for status using UUID or reference
    const checkId = marzpayTxId || currentTx?.reference_code;
    if (marzpayApiKey && marzpayApiSecret && checkId) {
      const basicAuth = btoa(`${marzpayApiKey}:${marzpayApiSecret}`);
      try {
        const marzCheckRes = await fetch(`${marzpayBaseUrl}/collect-money/${checkId}`, {
          method: "GET",
          headers: {
            "Authorization": `Basic ${basicAuth}`,
            "Accept": "application/json",
          },
        });
        if (marzCheckRes.ok) {
          const marzData = await marzCheckRes.json();
          const remoteStatus = (marzData.data?.transaction?.status || marzData.status || "").toUpperCase();
          if (remoteStatus === "COMPLETED" || remoteStatus === "SUCCESS" || remoteStatus === "PAID") {
            finalStatus = "COMPLETED";
          } else if (remoteStatus === "FAILED" || remoteStatus === "CANCELLED" || remoteStatus === "DECLINED") {
            finalStatus = "FAILED";
          } else if (remoteStatus) {
            finalStatus = remoteStatus;
          }
        }
      } catch (err: any) {
        console.error("Error polling MarzPay status:", err);
      }
    }

    // If transaction reached COMPLETED status, update the database & student plan
    if (finalStatus === "COMPLETED" && currentTx) {
      // 1. Update payments_transactions record
      await supabase
        .from("payments_transactions")
        .update({ status: "COMPLETED" })
        .eq("id", currentTx.id);

      // 2. Activate student plan in public.users
      const phoneToMatch = userPhone || currentTx.phone_number;
      const emailToMatch = userEmail;
      const planDuration = currentTx.plan_duration || "1 Month";

      const planUpdateData = {
        is_plan_active: true,
        plan_duration: planDuration,
        plan_expiry_date: "Valid for 30 days",
      };

      if (phoneToMatch) {
        await supabase
          .from("users")
          .update(planUpdateData)
          .eq("phone_number", phoneToMatch);
      }

      if (emailToMatch) {
        await supabase
          .from("users")
          .update(planUpdateData)
          .eq("email", emailToMatch);
      }
    } else if (finalStatus === "FAILED" && currentTx) {
      await supabase
        .from("payments_transactions")
        .update({ status: "FAILED" })
        .eq("id", currentTx.id);
    }

    const isCompleted = finalStatus === "COMPLETED" || finalStatus === "SUCCESS" || finalStatus === "PAID";
    const isFailed = finalStatus === "FAILED" || finalStatus === "CANCELLED" || finalStatus === "DECLINED" || finalStatus === "REJECTED";
    const isPending = !isCompleted && !isFailed;

    return new Response(
      JSON.stringify({
        success: true,
        status: finalStatus,
        is_completed: isCompleted,
        is_failed: isFailed,
        is_pending: isPending,
        transaction_id: currentTx?.id || txId,
        reference_code: currentTx?.reference_code || marzpayTxId,
        student_name: currentTx?.user_name || "Student",
        amount_ugx: currentTx?.amount_ugx || 0,
        provider: currentTx?.provider || "Mobile Money",
        error: isFailed ? "Payment was cancelled, declined or timed out on MarzPay." : null,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (error: any) {
    console.error("Fatal error in marzpay-check-status:", error);
    return new Response(
      JSON.stringify({ success: false, error: error.message || "Internal server error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
