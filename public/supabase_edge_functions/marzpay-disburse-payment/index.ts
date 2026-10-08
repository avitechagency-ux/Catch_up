import { createClient } from "npm:@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

function normalizeUgandanPhone(phone: string): string {
  let cleaned = phone.replace(/[\s\-\(\)]/g, "").trim();
  if (cleaned.startsWith("+256")) return cleaned;
  if (cleaned.startsWith("256")) return `+${cleaned}`;
  if (cleaned.startsWith("0")) return `+256${cleaned.substring(1)}`;
  if (cleaned.length === 9 && (cleaned.startsWith("7") || cleaned.startsWith("3"))) {
    return `+256${cleaned}`;
  }
  return cleaned;
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
    const actionType = (body.type || "Withdrew").trim();
    const recipient = (body.recipient || body.recipient_or_account || "").trim();
    const rawPhone = (body.phone_number || body.account_number || recipient).trim();
    const amountUgx = Number(body.amount_ugx || body.amount || 0);
    const note = body.note || body.description || `Admin ${actionType} Disbursement`;

    if (amountUgx <= 0) {
      return new Response(
        JSON.stringify({ success: false, error: "Invalid disbursement amount" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const normalizedPhone = normalizeUgandanPhone(rawPhone);
    const reference = crypto.randomUUID();
    const txId = `TX-${reference.substring(0, 8).toUpperCase()}`;

    let marzpayTxId = reference;
    let marzpayStatus = "PENDING";
    let marzpayRawResponse: any = null;

    if (marzpayApiKey && marzpayApiSecret) {
      const basicAuth = btoa(`${marzpayApiKey}:${marzpayApiSecret}`);
      const sendPayload = {
        amount: amountUgx,
        phone_number: normalizedPhone,
        reference: reference,
        country: "UG",
        description: note,
        metadata: [
          { orderId: txId },
          { recipient: recipient }
        ]
      };

      try {
        const marzResponse = await fetch(`${marzpayBaseUrl}/send-money`, {
          method: "POST",
          headers: {
            "Authorization": `Basic ${basicAuth}`,
            "Content-Type": "application/json",
            "Accept": "application/json",
          },
          body: JSON.stringify(sendPayload),
        });

        const marzJson = await marzResponse.json();
        marzpayRawResponse = marzJson;

        if (marzResponse.ok) {
          marzpayTxId = marzJson.data?.transaction?.uuid || marzJson.uuid || reference;
          marzpayStatus = (marzJson.data?.transaction?.status || marzJson.status || "PENDING").toUpperCase();
        } else {
          console.warn("MarzPay disbursement error (may require whitelist/disbursement permissions):", marzJson);
          return new Response(
            JSON.stringify({
              success: false,
              error: marzJson.message || "Disbursement failed with payment gateway",
              details: marzJson,
            }),
            { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
          );
        }
      } catch (err: any) {
        console.error("Disbursement fetch failed:", err);
      }
    }

    const supabase = createClient(supabaseUrl, supabaseServiceKey);
    const todayStr = new Intl.DateTimeFormat("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(new Date());

    const { error: dbError } = await supabase
      .from("payments_transactions")
      .upsert({
        id: txId,
        user_name: recipient || "Admin Disbursement",
        user_role: "Teacher / Recipient",
        phone_number: normalizedPhone,
        provider: "Bank / Mobile Money",
        category: "DISBURSEMENT",
        plan_duration: note,
        amount_ugx: -Math.abs(amountUgx),
        fee_ugx: 0,
        net_amount_ugx: -Math.abs(amountUgx),
        status: marzpayStatus === "COMPLETED" ? "COMPLETED" : "PENDING",
        reference_code: marzpayTxId,
        reference_number: reference,
        title: `Disbursement: ${actionType}`,
        description: note,
        transaction_date: todayStr,
        transaction_time: new Date().toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" }),
      });

    if (dbError) {
      console.error("Disbursement DB save error:", dbError);
    }

    return new Response(
      JSON.stringify({
        success: true,
        message: "Disbursement transaction recorded",
        transaction_id: txId,
        reference: reference,
        status: marzpayStatus,
        details: marzpayRawResponse,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (error: any) {
    console.error("Fatal error in marzpay-disburse-payment:", error);
    return new Response(
      JSON.stringify({ success: false, error: error.message || "Internal server error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
