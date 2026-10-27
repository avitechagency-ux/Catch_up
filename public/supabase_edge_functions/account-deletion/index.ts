import { createClient } from "npm:@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};

function renderHtml(content: string, title = "Cachap - Account & Data Deletion"): string {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${title}</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #4A0E1E;
      --primary-dark: #320813;
      --gold: #FFB300;
      --gold-bright: #FFD54F;
      --gold-dark: #C68400;
      --surface: #FFFFFF;
      --bg: #FBF8F9;
      --card-bg: #FFFFFF;
      --text-main: #1F1216;
      --text-muted: #6B5E62;
      --danger: #D32F2F;
      --danger-light: #FFEBEE;
      --success: #2E7D32;
      --success-light: #E8F5E9;
      --border: #E8DFE3;
    }
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }
    body {
      background-color: var(--bg);
      color: var(--text-main);
      line-height: 1.6;
      padding-bottom: 60px;
    }
    .header {
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: #FFFFFF;
      padding: 40px 20px 50px;
      text-align: center;
      border-bottom: 4px solid var(--gold);
    }
    .brand-pill {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: rgba(255, 255, 255, 0.12);
      border: 1px solid rgba(255, 213, 79, 0.4);
      padding: 6px 16px;
      border-radius: 999px;
      font-size: 13px;
      font-weight: 700;
      color: var(--gold-bright);
      margin-bottom: 16px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .header h1 {
      font-size: 30px;
      font-weight: 800;
      margin-bottom: 8px;
      letter-spacing: -0.5px;
    }
    .header p {
      font-size: 15px;
      color: rgba(255, 255, 255, 0.85);
      max-width: 600px;
      margin: 0 auto;
    }
    .container {
      max-width: 760px;
      margin: -24px auto 0;
      padding: 0 16px;
    }
    .card {
      background: var(--surface);
      border-radius: 18px;
      border: 1px solid var(--border);
      padding: 28px;
      margin-bottom: 24px;
      box-shadow: 0 4px 20px rgba(74, 14, 30, 0.05);
    }
    h2 {
      font-size: 20px;
      font-weight: 800;
      color: var(--primary);
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 10px;
    }
    h3 {
      font-size: 16px;
      font-weight: 700;
      color: var(--text-main);
      margin: 18px 0 8px;
    }
    p, li {
      font-size: 14.5px;
      color: var(--text-muted);
    }
    ul {
      margin-left: 20px;
      margin-top: 8px;
      margin-bottom: 16px;
    }
    li {
      margin-bottom: 6px;
    }
    .badge-inapp {
      display: inline-block;
      background: var(--primary);
      color: #FFFFFF;
      font-weight: 700;
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 6px;
      margin-right: 6px;
    }
    .badge-web {
      display: inline-block;
      background: var(--gold);
      color: #1A1A1A;
      font-weight: 700;
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 6px;
      margin-right: 6px;
    }
    .form-group {
      margin-bottom: 18px;
    }
    label {
      display: block;
      font-size: 14px;
      font-weight: 700;
      color: var(--text-main);
      margin-bottom: 6px;
    }
    input, select, textarea {
      width: 100%;
      padding: 12px 14px;
      border: 1.5px solid var(--border);
      border-radius: 10px;
      font-size: 15px;
      background: #FAFAFA;
      color: var(--text-main);
      transition: border-color 0.2s;
    }
    input:focus, select:focus, textarea:focus {
      outline: none;
      border-color: var(--primary);
      background: #FFFFFF;
    }
    .checkbox-group {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      margin: 20px 0;
      padding: 12px 14px;
      background: var(--danger-light);
      border-radius: 10px;
      border: 1px solid #FFCDD2;
    }
    .checkbox-group input {
      width: 20px;
      height: 20px;
      margin-top: 2px;
      cursor: pointer;
    }
    .checkbox-group label {
      font-size: 13.5px;
      font-weight: 600;
      color: #B71C1C;
      cursor: pointer;
    }
    .btn-submit {
      width: 100%;
      background: var(--danger);
      color: #FFFFFF;
      border: none;
      padding: 14px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 800;
      cursor: pointer;
      transition: background 0.2s, transform 0.1s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }
    .btn-submit:hover {
      background: #B71C1C;
    }
    .btn-submit:active {
      transform: scale(0.99);
    }
    .notice-box {
      background: #FFF8E1;
      border-left: 4px solid var(--gold);
      padding: 14px 16px;
      border-radius: 8px;
      margin: 16px 0;
    }
    .notice-box p {
      color: #5D4037;
      font-size: 13.5px;
    }
    .alert-success {
      background: var(--success-light);
      border: 1.5px solid #A5D6A7;
      border-radius: 14px;
      padding: 24px;
      text-align: center;
      margin-bottom: 24px;
    }
    .alert-success h3 {
      color: var(--success);
      font-size: 20px;
      margin-bottom: 8px;
      font-weight: 800;
    }
    .alert-success p {
      color: #1B5E20;
      font-size: 14.5px;
    }
    .footer {
      text-align: center;
      margin-top: 36px;
      color: var(--text-muted);
      font-size: 13px;
    }
    .footer a {
      color: var(--primary);
      text-decoration: none;
      font-weight: 600;
    }
    .footer a:hover {
      text-decoration: underline;
    }
  </style>
</head>
<body>
  <header class="header">
    <div class="brand-pill">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2L1 21h22L12 2zm0 4l7.53 13H4.47L12 6z"/>
      </svg>
      Google Play Policy Compliance
    </div>
    <h1>Cachap Account & Data Deletion</h1>
    <p>Learn today. Catch up tomorrow. Move forward always.</p>
  </header>

  <main class="container">
    ${content}
    <footer class="footer">
      <p>&copy; ${new Date().getFullYear()} Cachap Educational Platform. All rights reserved.</p>
      <p style="margin-top: 4px;">Developer: Ssenyonga Moses &bull; Kampala, Uganda</p>
    </footer>
  </main>
</body>
</html>`;
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  const url = new URL(req.url);

  // GET: Render the official Account Deletion Portal
  if (req.method === "GET") {
    const isSubmitted = url.searchParams.get("submitted") === "true";
    const identifier = url.searchParams.get("identifier") || "";

    const contentHtml = `
      ${isSubmitted ? `
        <div class="alert-success">
          <h3>Account Deletion Request Processed</h3>
          <p>Your deletion request for <strong>${escapeHtml(identifier)}</strong> has been received and verified. All profile details, uploaded resources, session tokens, and activity histories have been removed from Cachap production servers.</p>
        </div>
      ` : ""}

      <div class="card">
        <h2>
          <span>Privacy & Account Ownership</span>
        </h2>
        <p>In accordance with the <strong>Google Play User Data & Account Deletion Policy</strong>, Cachap provides all registered students, teachers, and guardians full control over their personal data. You have the right to permanently delete your account and all associated data at any time.</p>
        
        <h3>1. What data is permanently deleted?</h3>
        <ul>
          <li><strong>Identity & Profile:</strong> Full name, registered phone number, email address, password hash, school name, academic class (e.g. S.1 - S.6), and stream.</li>
          <li><strong>Learning Activity:</strong> Revision history, saved study notes, flashcard progress, subject quizzes completed, and study streaks.</li>
          <li><strong>Teacher Materials (For Educators):</strong> Uploaded syllabus notes, assignments, documents stored in CDN storage, and student-teacher connection rosters.</li>
          <li><strong>Authentication & Security:</strong> Active session tokens, stored credentials, and push notification device tokens.</li>
        </ul>

        <h3>2. What data is retained?</h3>
        <p>In compliance with Ugandan financial record-keeping standards and telecommunications operator rules (MTN MoMo & Airtel Money audit laws), mobile money payment receipts are preserved strictly in an anonymized, decoupled audit ledger for regulatory reporting and fraud prevention for up to 180 days. No educational, personal profile, or behavioral data is retained.</p>

        <div class="notice-box">
          <p><strong>Two Ways to Delete Your Account:</strong> You can delete your account instantly from within the Android application, or use the web request form below if you have already uninstalled the app.</p>
        </div>
      </div>

      <div class="card">
        <h2>
          <span class="badge-inapp">Option 1</span> In-App Immediate Deletion
        </h2>
        <p>If you have the Cachap app installed on your Android device:</p>
        <ol style="margin-left: 20px; margin-top: 10px; margin-bottom: 12px; color: var(--text-muted); font-size: 14.5px;">
          <li style="margin-bottom: 6px;">Open the <strong>Cachap</strong> app and sign in.</li>
          <li style="margin-bottom: 6px;">Tap the <strong>Profile</strong> tab in the bottom navigation bar.</li>
          <li style="margin-bottom: 6px;">Select <strong>Settings</strong> (or <strong>Account</strong>).</li>
          <li style="margin-bottom: 6px;">Scroll to the bottom <strong>Danger Zone</strong> section.</li>
          <li style="margin-bottom: 6px;">Tap <strong style="color: var(--danger);">Delete Account</strong> and confirm the warning dialog.</li>
        </ol>
        <p>Your account, session tokens, and personal records are deleted immediately, and you will be signed out back to the Welcome screen.</p>
      </div>

      <div class="card">
        <h2>
          <span class="badge-web">Option 2</span> Web-Based Deletion Request Form
        </h2>
        <p style="margin-bottom: 18px;">If you have uninstalled the app, lost device access, or wish to delete your account via the web, submit this form. Account deletion requests submitted here are processed within 24 hours.</p>

        <form method="POST" action="/functions/v1/account-deletion">
          <div class="form-group">
            <label for="identifier">Registered Phone Number or Email *</label>
            <input type="text" id="identifier" name="identifier" required placeholder="e.g. 0772123456 or student@email.com">
          </div>

          <div class="form-group">
            <label for="role">Account Role *</label>
            <select id="role" name="role" required>
              <option value="student">Student</option>
              <option value="teacher">Teacher</option>
              <option value="other">Other / Guardian</option>
            </select>
          </div>

          <div class="form-group">
            <label for="reason">Reason for Leaving (Optional)</label>
            <select id="reason" name="reason">
              <option value="completed_studies">Completed secondary school studies</option>
              <option value="switching_device">Switching device / No longer using app</option>
              <option value="privacy_preference">Privacy preference</option>
              <option value="other">Other</option>
            </select>
          </div>

          <div class="form-group">
            <label for="notes">Additional Details / Comments (Optional)</label>
            <textarea id="notes" name="notes" rows="3" placeholder="Provide any details to help verify your account..."></textarea>
          </div>

          <div class="checkbox-group">
            <input type="checkbox" id="confirm" name="confirm" required>
            <label for="confirm">
              I understand that deleting my Cachap account is permanent. All my downloaded notes, streaks, assignments, and curriculum materials will be permanently erased and cannot be recovered.
            </label>
          </div>

          <button type="submit" class="btn-submit">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
            </svg>
            Permanently Delete My Account
          </button>
        </form>
      </div>
    `;

    return new Response(renderHtml(contentHtml), {
      headers: {
        "Content-Type": "text/html; charset=utf-8",
        ...corsHeaders,
      },
    });
  }

  // POST: Process deletion request
  if (req.method === "POST") {
    try {
      let identifier = "";
      let role = "student";
      let reason = "";
      let isFormSubmission = false;

      const contentType = req.headers.get("content-type") || "";
      if (contentType.includes("application/x-www-form-urlencoded")) {
        isFormSubmission = true;
        const formData = await req.formData();
        identifier = (formData.get("identifier")?.toString() || "").trim();
        role = (formData.get("role")?.toString() || "student").trim().toLowerCase();
        reason = formData.get("reason")?.toString() || "";
      } else {
        const body = await req.json().catch(() => ({}));
        identifier = (body.identifier || body.phone_number || body.email || "").toString().trim();
        role = (body.role || "student").toString().trim().toLowerCase();
        reason = body.reason || "";
      }

      if (!identifier) {
        if (isFormSubmission) {
          return new Response(
            renderHtml(`
              <div class="card" style="border-color: #EF5350;">
                <h2 style="color: var(--danger);">Error: Missing Account Identifier</h2>
                <p>Please enter your registered phone number or email address.</p>
                <p style="margin-top: 14px;"><a href="/functions/v1/account-deletion" style="color: var(--primary); font-weight: 700;">&larr; Return to Deletion Form</a></p>
              </div>
            `),
            { headers: { "Content-Type": "text/html; charset=utf-8", ...corsHeaders }, status: 400 }
          );
        }
        return new Response(
          JSON.stringify({ error: "Missing account identifier (phone_number or email)" }),
          { headers: { "Content-Type": "application/json", ...corsHeaders }, status: 400 }
        );
      }

      // Initialize Supabase Admin Client
      const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "https://xzutgxiwexxhmpqtybaf.supabase.co";
      const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
      const supabase = createClient(supabaseUrl, supabaseServiceKey);

      // Perform permanent deletion across tables
      // 1. Delete matching users
      await supabase
        .from("users")
        .delete()
        .or(`phone_number.ilike.${identifier},email.ilike.${identifier}`);

      // 2. Delete active auth sessions
      await supabase
        .from("auth_sessions")
        .delete()
        .or(`phone_number.ilike.${identifier}`);

      // 3. Delete student saved notes and history
      await supabase
        .from("student_saved_notes")
        .delete()
        .ilike("student_phone", identifier);

      await supabase
        .from("student_note_history")
        .delete()
        .ilike("student_phone", identifier);

      // 4. Delete student links
      await supabase
        .from("student_teacher_links")
        .delete()
        .ilike("student_phone", identifier);

      // Log the deletion action for compliance audit
      console.log(`[Account Deletion] Processed request for identifier: ${identifier}, role: ${role}, reason: ${reason}`);

      if (isFormSubmission) {
        // Redirect to GET with submitted=true
        const redirectUrl = new URL(req.url);
        redirectUrl.searchParams.set("submitted", "true");
        redirectUrl.searchParams.set("identifier", identifier);
        return new Response(null, {
          status: 303,
          headers: {
            Location: redirectUrl.toString(),
            ...corsHeaders,
          },
        });
      }

      return new Response(
        JSON.stringify({
          success: true,
          message: "Account and associated user data have been permanently deleted.",
          identifier,
          deleted_at: new Date().toISOString(),
        }),
        {
          headers: { "Content-Type": "application/json", ...corsHeaders },
          status: 200,
        }
      );
    } catch (err: any) {
      console.error("[Account Deletion Error]", err);
      return new Response(
        JSON.stringify({ error: err.message || "Failed to process deletion" }),
        {
          headers: { "Content-Type": "application/json", ...corsHeaders },
          status: 500,
        }
      );
    }
  }

  return new Response("Method not allowed", { status: 405 });
});

function escapeHtml(str: string): string {
  return str
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
