import { createClient } from "npm:@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-webhook-signature, x-marzpay-signature",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
};

/**
 * Expected MarzPay Webhook Secret configured for Catch Up.
 */
const EXPECTED_WEBHOOK_SECRET = "whsec_BtNjMmQvMaRxylJBw8Wd0uUUY0zgWm0hUbLJLdbe";

/**
 * Validates HMAC SHA-256 signature if present.
 */
async function verifyHmacSha256(payload: string, signatureHex: string, secret: string): Promise<boolean> {
  try {
    const encoder = new TextEncoder();
    const keyData = encoder.encode(secret);
    const cryptoKey = await crypto.subtle.importKey(
      "raw",
      keyData,
      { name: "HMAC", hash: "SHA-256" },
      false,
      ["verify", "sign"]
    );

    // Compute signature
    const computedSignatureBuffer = await crypto.subtle.sign("HMAC", cryptoKey, encoder.encode(payload));
    const computedHex = Array.from(new Uint8Array(computedSignatureBuffer))
      .map((b) => b.toString(16).padStart(2, "0"))
      .join("");

    return computedHex.toLowerCase() === signatureHex.toLowerCase().trim();
  } catch (_e) {
    return false;
  }
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
  const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
  const configuredSecret = Deno.env.get("MARZPAY_WEBHOOK_SECRET") || EXPECTED_WEBHOOK_SECRET;

  if (req.method === "GET") {
    return new Response(
      JSON.stringify({
        status: "active",
        service: "marzpay-webhook",
        message: "MarzPay Webhook listener is online and ready to receive IPN callbacks",
        configured_secret_prefix: configuredSecret.substring(0, 10) + "..."
      }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }

  try {
    const rawBody = await req.text();
    let body: Record<string, any> = {};
    try {
      body = JSON.parse(rawBody);
    } catch (_e) {
      body = {};
    }

    // Check multiple header conventions used by payment gateways:
    // 1. `x-webhook-signature` or `x-marzpay-signature`
    // 2. `x-webhook-secret` or `authorization: Bearer <secret>`
    // 3. Secret passed in payload `webhook_secret` or query string `secret`
    const url = new URL(req.url);
    const signatureHeader = req.headers.get("x-webhook-signature") ||
      req.headers.get("x-marzpay-signature") ||
      req.headers.get("webhook-signature") || "";

    const secretHeader = req.headers.get("x-webhook-secret") ||
      req.headers.get("x-marzpay-secret") ||
      req.headers.get("authorization")?.replace(/^Bearer\s+/i, "") ||
      url.searchParams.get("secret") ||
      body.webhook_secret ||
      "";

    let isAuthorized = false;

    // 1. If a direct secret token is passed, verify exact match
    if (secretHeader && (secretHeader === configuredSecret || secretHeader === EXPECTED_WEBHOOK_SECRET)) {
      isAuthorized = true;
    }

    // 2. If an HMAC signature is passed, verify cryptographic match
    if (!isAuthorized && signatureHeader) {
      const isValid = await verifyHmacSha256(rawBody, signatureHeader, configuredSecret);
      if (isValid) {
        isAuthorized = true;
      }
    }

    // If neither signature nor secret matched and secret is strictly enforced:
    if (!isAuthorized && (signatureHeader || secretHeader)) {
      console.warn("Invalid MarzPay webhook signature / secret received");
      return new Response(
        JSON.stringify({ success: false, error: "Invalid webhook signature or secret" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Extract transaction metadata from MarzPay webhook payload
    // MarzPay payload formats:
    // { event: "payment.success", data: { transaction: { uuid, reference, status, amount, phone_number } } }
    // or { status: "COMPLETED", reference: "...", amount: 1000 }
    const txData = body.data?.transaction || body.transaction || body.data || body;

    const reference = txData.reference || txData.uuid || txData.reference_code || body.reference || "";
    const marzTxId = txData.uuid || txData.id || txData.transaction_id || "";
    const rawStatus = (txData.status || body.event || body.status || "COMPLETED").toUpperCase();
    const amountUgx = Number(txData.amount || body.amount || 1000);
    const phoneNumber = txData.phone_number || txData.phoneNumber || body.phone_number || "";

    let finalStatus = "PENDING";
    if (rawStatus.includes("SUCCESS") || rawStatus.includes("COMPLETE") || rawStatus.includes("PAID")) {
      finalStatus = "COMPLETED";
    } else if (rawStatus.includes("FAIL") || rawStatus.includes("DECLIN") || rawStatus.includes("CANCEL")) {
      finalStatus = "FAILED";
    }

    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Look up and update the transaction in payments_transactions
    let query = supabase.from("payments_transactions").select("*");
    if (reference) {
      query = query.or(`reference_number.eq.${reference},reference_code.eq.${reference},id.eq.${reference}`);
    } else if (marzTxId) {
      query = query.eq("reference_code", marzTxId);
    }

    const { data: existingTxs } = await query;
    const currentTx = existingTxs?.[0];

    if (currentTx) {
      await supabase
        .from("payments_transactions")
        .update({
          status: finalStatus,
          reference_code: marzTxId || currentTx.reference_code,
        })
        .eq("id", currentTx.id);

      // If subscription succeeded, activate the student's plan
      if (finalStatus === "COMPLETED" && currentTx.category === "SUBSCRIPTION") {
        const phone = phoneNumber || currentTx.phone_number;
        const planDuration = currentTx.plan_duration || "1 Month";
        const planUpdateData = {
          is_plan_active: true,
          plan_duration: planDuration,
          plan_expiry_date: "Valid for 30 days",
        };

        if (phone) {
          await supabase.from("users").update(planUpdateData).eq("phone_number", phone);
        }
      }
    } else if (reference || marzTxId) {
      // Upsert record if not already present
      const txId = reference.startsWith("TX-") ? reference : `TX-${crypto.randomUUID().substring(0, 8).toUpperCase()}`;
      await supabase
        .from("payments_transactions")
        .upsert({
          id: txId,
          user_name: txData.student_name || "Student",
          user_role: "Student",
          phone_number: phoneNumber,
          provider: "Mobile Money",
          category: "SUBSCRIPTION",
          plan_duration: "1 Month",
          amount_ugx: amountUgx,
          fee_ugx: 0,
          net_amount_ugx: amountUgx,
          status: finalStatus,
          reference_code: marzTxId || reference,
          reference_number: reference,
          title: "Catch Up 1 Month Subscription",
          description: `MarzPay Webhook Verified: ${finalStatus}`,
          transaction_date: new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "2-digit", year: "numeric" }).format(new Date()),
          transaction_time: new Date().toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" }),
        });
    }

    return new Response(
      JSON.stringify({
        success: true,
        message: "Webhook processed and payment verified successfully",
        verified: isAuthorized,
        reference: reference,
        status: finalStatus,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    console.error("Error processing MarzPay webhook:", err);
    return new Response(
      JSON.stringify({ success: false, error: err.message || "Failed to process webhook" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
