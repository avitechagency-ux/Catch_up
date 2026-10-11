# MarzPay + Supabase Edge Functions Deployment Guide

Catch Up connects student subscriptions (UGX 1,000 / month) and Admin disbursements to mobile money (MTN MoMo & Airtel Money) via MarzPay through Supabase Edge Functions.

## 1. Secrets Configuration

Store your MarzPay and Supabase service credentials in Supabase Edge Functions secrets (never in the Android APK).

In your terminal with Supabase CLI:

```bash
supabase secrets set MARZPAY_API_USER="your-marzpay-api-user"
supabase secrets set MARZPAY_API_KEY="your-marzpay-api-key"
supabase secrets set MARZPAY_BASE_URL="https://wallet.wearemarz.com/api/v1"
supabase secrets set MARZPAY_WEBHOOK_SECRET="whsec_BtNjMmQvMaRxylJBw8Wd0uUUY0zgWm0hUbLJLdbe"
```

Or configure them in the Supabase Dashboard under:
**Project Settings -> Edge Functions -> Secrets**

## 2. Deploying the Functions

Deploy the functions to your Supabase project:

```bash
# 1. Create Payment Function (STK Push initiation)
supabase functions deploy marzpay-create-payment --no-verify-jwt

# 2. Check Payment Status Function (Realtime / Polling verification)
supabase functions deploy marzpay-check-status --no-verify-jwt

# 3. Disburse Funds Function (Admin Send / Withdraw to mobile money)
supabase functions deploy marzpay-disburse-payment --no-verify-jwt

# 4. Webhook IPN Listener (Verifies incoming payment webhooks with secret)
supabase functions deploy marzpay-webhook --no-verify-jwt
```

## 3. Function Endpoints

Once deployed, the functions will be accessible at:
- `https://<your-project>.supabase.co/functions/v1/marzpay-create-payment`
- `https://<your-project>.supabase.co/functions/v1/marzpay-check-status`
- `https://<your-project>.supabase.co/functions/v1/marzpay-disburse-payment`
- `https://<your-project>.supabase.co/functions/v1/marzpay-webhook` (Webhook Secret: `whsec_BtNjMmQvMaRxylJBw8Wd0uUUY0zgWm0hUbLJLdbe`)

The Android app will automatically call these endpoints and synchronize real-time status updates directly into `public.payments_transactions` and `public.users`.

---

# Cloudflare R2 Storage Integration

Catch Up uses Cloudflare R2 for zero-egress fee high-speed note uploads and global CDN distribution.

## Secrets Configured in Supabase:
- `R2_ACCOUNT_ID`: `63d266d137630ce0e2ce572125fc70a0`
- `R2_BUCKET_NAME`: `catchup-notes-storage`
- `R2_ACCESS_KEY_ID`: `345ba99659537452f3151856e1ec8b3e`
- `R2_SECRET_ACCESS_KEY`: `a8d8b2035648e7487e89fe81cf83513d24293f8fc47af1e927ab6ec80ed81c98`
- `R2_PUBLIC_URL`: `https://pub-ea81114114a4490db50d78b42b952f05.r2.dev`
- `R2_ENDPOINT`: `https://63d266d137630ce0e2ce572125fc70a0.r2.cloudflarestorage.com`

## Edge Function: `r2-storage`
Endpoint: `https://xzutgxiwexxhmpqtybaf.supabase.co/functions/v1/r2-storage`

Actions:
- `action: "get-upload-url"`: Generates an S3 presigned PUT URL for direct client upload to Cloudflare R2.
- `action: "upload"`: Direct binary/base64 upload through edge function if needed.
- `action: "get-download-url"`: Generates temporary presigned GET URL for secured objects.
- `action: "delete"`: Removes files from R2.
- `action: "info"`: Returns storage status and public CDN base URL.

