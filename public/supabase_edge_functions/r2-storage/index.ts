import { S3Client, PutObjectCommand, GetObjectCommand, DeleteObjectCommand } from "npm:@aws-sdk/client-s3@3.540.0";
import { getSignedUrl } from "npm:@aws-sdk/s3-request-presigner@3.540.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
};

function getS3Client(): { s3: S3Client; bucketName: string; publicBaseUrl: string } {
  const accountId = Deno.env.get("R2_ACCOUNT_ID") || "63d266d137630ce0e2ce572125fc70a0";
  const accessKeyId = Deno.env.get("R2_ACCESS_KEY_ID") || "";
  const secretAccessKey = Deno.env.get("R2_SECRET_ACCESS_KEY") || "";
  const bucketName = Deno.env.get("R2_BUCKET_NAME") || "catchup-notes-storage";
  const publicBaseUrl = (Deno.env.get("R2_PUBLIC_URL") || "https://pub-ea81114114a4490db50d78b42b952f05.r2.dev").replace(/\/$/, "");

  const endpoint = Deno.env.get("R2_ENDPOINT") || `https://${accountId}.r2.cloudflarestorage.com`;

  const s3 = new S3Client({
    region: "auto",
    endpoint: endpoint,
    credentials: {
      accessKeyId: accessKeyId,
      secretAccessKey: secretAccessKey,
    },
  });

  return { s3, bucketName, publicBaseUrl };
}

function sanitizePath(raw: string): string {
  return raw
    .replace(/^\/+/, "")
    .replace(/[^a-zA-Z0-9._\-\/]/g, "_");
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const { s3, bucketName, publicBaseUrl } = getS3Client();

    const url = new URL(req.url);
    let body: Record<string, any> = {};

    if (req.method === "POST") {
      try {
        body = await req.json();
      } catch (_e) {
        body = {};
      }
    }

    const action = body.action || url.searchParams.get("action") || "get-upload-url";

    if (action === "info") {
      return new Response(
        JSON.stringify({
          status: "success",
          provider: "Cloudflare R2",
          bucket: bucketName,
          publicBaseUrl: publicBaseUrl,
          message: "Cloudflare R2 storage operational"
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (action === "get-upload-url") {
      const teacherCode = (body.teacherCode || url.searchParams.get("teacherCode") || "COMMON").toUpperCase();
      const rawFileName = body.fileName || url.searchParams.get("fileName") || `note_${Date.now()}.docx`;
      const sanitizedFileName = sanitizePath(rawFileName);
      const contentType = body.contentType || "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      
      const folder = body.folder || (body.category ? `study_materials/${sanitizePath(body.category)}` : null);
      const key = body.key 
        ? sanitizePath(body.key) 
        : (folder ? `${sanitizePath(folder)}/${sanitizedFileName}` : `notes_files/${teacherCode}/${sanitizedFileName}`);

      const command = new PutObjectCommand({
        Bucket: bucketName,
        Key: key,
        ContentType: contentType,
      });

      // Generate presigned PUT URL valid for 15 minutes (900 seconds)
      const uploadUrl = await getSignedUrl(s3, command, { expiresIn: 900 });
      const publicUrl = `${publicBaseUrl}/${key}`;

      return new Response(
        JSON.stringify({
          status: "success",
          action: "get-upload-url",
          uploadUrl,
          publicUrl,
          key,
          bucket: bucketName,
          expiresInSeconds: 900
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (action === "upload") {
      // Direct base64 upload to R2 via edge function
      const teacherCode = (body.teacherCode || url.searchParams.get("teacherCode") || "COMMON").toUpperCase();
      const rawFileName = body.fileName || url.searchParams.get("fileName") || `note_${Date.now()}.docx`;
      const sanitizedFileName = sanitizePath(rawFileName);
      const contentType = body.contentType || "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      const folder = body.folder || (body.category ? `study_materials/${sanitizePath(body.category)}` : null);
      const key = body.key 
        ? sanitizePath(body.key) 
        : (folder ? `${sanitizePath(folder)}/${sanitizedFileName}` : `notes_files/${teacherCode}/${sanitizedFileName}`);

      const base64Content = body.fileBase64 || body.base64 || "";
      if (!base64Content) {
        return new Response(
          JSON.stringify({ error: "Missing fileBase64 data in request body" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      // Convert base64 to Uint8Array
      const binaryString = atob(base64Content);
      const bytes = new Uint8Array(binaryString.length);
      for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
      }

      const command = new PutObjectCommand({
        Bucket: bucketName,
        Key: key,
        Body: bytes,
        ContentType: contentType,
      });

      await s3.send(command);

      const publicUrl = `${publicBaseUrl}/${key}`;

      return new Response(
        JSON.stringify({
          status: "success",
          action: "upload",
          publicUrl,
          key,
          bytesUploaded: bytes.length,
          bucket: bucketName
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (action === "get-download-url") {
      const key = sanitizePath(body.key || url.searchParams.get("key") || "");
      if (!key) {
        return new Response(
          JSON.stringify({ error: "Missing key parameter" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const command = new GetObjectCommand({
        Bucket: bucketName,
        Key: key,
      });

      const downloadUrl = await getSignedUrl(s3, command, { expiresIn: 3600 });
      const publicUrl = `${publicBaseUrl}/${key}`;

      return new Response(
        JSON.stringify({
          status: "success",
          action: "get-download-url",
          downloadUrl,
          publicUrl,
          key,
          bucket: bucketName
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    if (action === "delete") {
      const key = sanitizePath(body.key || url.searchParams.get("key") || "");
      if (!key) {
        return new Response(
          JSON.stringify({ error: "Missing key parameter" }),
          { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }

      const command = new DeleteObjectCommand({
        Bucket: bucketName,
        Key: key,
      });

      await s3.send(command);

      return new Response(
        JSON.stringify({
          status: "success",
          action: "delete",
          key,
          bucket: bucketName
        }),
        { headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    return new Response(
      JSON.stringify({ error: `Unknown action '${action}'` }),
      { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );

  } catch (error: any) {
    console.error("R2 Storage error:", error);
    return new Response(
      JSON.stringify({
        error: error.message || "Internal server error in R2 storage function",
        stack: error.stack
      }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
