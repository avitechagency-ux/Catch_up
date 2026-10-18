import { createClient } from "npm:@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

/**
 * Normalizes phone numbers to standard E.164 Ugandan format (+256XXXXXXXXX).
 */
function normalizeUgandanPhone(phone: string): string {
  let cleaned = phone.replace(/[\s\-\(\)]/g, "").trim();
  if (cleaned.startsWith("+256")) {
    return cleaned;
  }
  if (cleaned.startsWith("256")) {
    return `+${cleaned}`;
  }
  if (cleaned.startsWith("0")) {
    return `+256${cleaned.substring(1)}`;
  }
  if (cleaned.length === 9 && (cleaned.startsWith("7") || cleaned.startsWith("3"))) {
    return `+256${cleaned}`;
  }
  return cleaned;
}

/**
 * Detects mobile money network provider based on phone number.
 */
function detectProvider(phone: string): string {
  const norm = normalizeUgandanPhone(phone);
  // Airtel Uganda prefixes: +25670, +25674, +25675
  if (norm.startsWith("+25670") || norm.startsWith("+25674") || norm.startsWith("+25675")) {
    return "Airtel Money";
  }
  // MTN Uganda prefixes: +25677, +25678, +25676, +25639
  return "MTN MoMo";
}

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

    const body = await req.json();
    const rawPhone = body.phone_number || body.phoneNumber || "";
    const paymentType = body.payment_type || body.paymentType || "subscription";
    const studentName = body.student_name || body.studentName || "Student";
    const userEmail = body.email || body.userEmail || "";
    const planDuration = body.plan_duration || body.planDuration || "1 Month";
    const amountUgx = Number(body.amount || body.amount_ugx || 2000);

    if (!rawPhone) {
      return new Response(
        JSON.stringify({ success: false, error: "Phone number is required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const normalizedPhone = normalizeUgandanPhone(rawPhone);
    const provider = detectProvider(normalizedPhone);

    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Unique UUID v4 reference for MarzPay API
    const reference = crypto.randomUUID();
    const txId = `TX-${reference.substring(0, 8).toUpperCase()}`;

    // Call MarzPay REST API if credentials are provided
    let marzpayTxId = reference;
    let marzpayStatus = "PENDING";
    let marzpayRawResponse: any = null;

    if (marzpayApiKey && marzpayApiSecret) {
      const basicAuth = btoa(`${marzpayApiKey}:${marzpayApiSecret}`);
      const metadataList: any[] = [
        { orderId: txId },
        { studentName: studentName || "Student" },
        { planDuration: planDuration },
        { provider: provider }
      ];
      if (userEmail) {
        metadataList.push({ userEmail: userEmail, isPII: true });
      }

      const marzpayPayload = {
        amount: amountUgx,
        phone_number: normalizedPhone,
        reference: reference,
        country: "UG",
        description: `Catch Up ${planDuration} subscription for ${studentName}`,
        metadata: metadataList,
      };

      try {
        const marzResponse = await fetch(`${marzpayBaseUrl}/collect-money`, {
          method: "POST",
          headers: {
            "Authorization": `Basic ${basicAuth}`,
            "Content-Type": "application/json",
            "Accept": "application/json",
          },
          body: JSON.stringify(marzpayPayload),
        });

        const marzJson = await marzResponse.json();
        marzpayRawResponse = marzJson;

        if (!marzResponse.ok) {
          console.error("MarzPay API error:", marzResponse.status, marzJson);
          return new Response(
            JSON.stringify({
              success: false,
              error: marzJson.message || marzJson.error || "Failed to initiate mobile money STK push with MarzPay",
              details: marzJson,
            }),
            { status: 502, headers: { ...corsHeaders, "Content-Type": "application/json" } }
          );
        }

        marzpayTxId = marzJson.data?.transaction?.uuid || marzJson.uuid || reference;
        marzpayStatus = (marzJson.data?.transaction?.status || marzJson.status || "PROCESSING").toUpperCase();
      } catch (err: any) {
        console.error("Fetch to MarzPay failed:", err);
        return new Response(
          JSON.stringify({
            success: false,
            error: `Network error connecting to MarzPay: ${err.message}`,
          }),
          { status: 502, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }
    } else {
      console.warn("MARZPAY credentials not configured. Running in fallback mode with reference:", reference);
    }

    // Save initial transaction in public.payments_transactions as PENDING
    const todayStr = new Intl.DateTimeFormat("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(new Date());

    const { error: dbError } = await supabase
      .from("payments_transactions")
      .upsert({
        id: txId,
        user_name: studentName,
        user_role: "Student",
        phone_number: normalizedPhone,
        provider: provider,
        category: "SUBSCRIPTION",
        plan_duration: planDuration,
        amount_ugx: amountUgx,
        fee_ugx: 0,
        net_amount_ugx: amountUgx,
        status: marzpayStatus === "COMPLETED" ? "COMPLETED" : "PENDING",
        reference_code: marzpayTxId,
        reference_number: reference,
        title: `Catch Up ${planDuration} Subscription`,
        description: `Catch Up ${planDuration} subscription for ${studentName}`,
        transaction_date: todayStr,
        transaction_time: new Date().toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" }),
      });

    if (dbError) {
      console.error("Database insert error:", dbError);
    }

    return new Response(
      JSON.stringify({
        success: true,
        message: "STK push initiated successfully. Please check your phone for the PIN prompt.",
        transaction_id: txId,
        reference: reference,
        marzpay_tx_id: marzpayTxId,
        status: marzpayStatus,
        provider: provider,
        phone_number: normalizedPhone,
        amount_ugx: amountUgx,
        raw_marzpay: marzpayRawResponse,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (error: any) {
    console.error("Fatal error in marzpay-create-payment:", error);
    return new Response(
      JSON.stringify({ success: false, error: error.message || "Internal server error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
