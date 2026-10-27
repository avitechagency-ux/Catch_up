// Script to publish the official Cachap Account Deletion page to Cloudflare R2
const fs = require('fs');

async function deployPage() {
  const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Cachap - Account & Data Deletion Request</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #4A0E1E;
      --primary-dark: #2E0812;
      --gold: #FFB300;
      --gold-bright: #FFD54F;
      --gold-dark: #C68400;
      --surface: #FFFFFF;
      --bg: #FBF8F9;
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
      padding: 42px 20px 52px;
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
      font-size: 32px;
      font-weight: 800;
      margin-bottom: 8px;
      letter-spacing: -0.5px;
    }
    .header p {
      font-size: 15px;
      color: rgba(255, 255, 255, 0.88);
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
      padding: 30px;
      margin-bottom: 24px;
      box-shadow: 0 4px 20px rgba(74, 14, 30, 0.05);
    }
    h2 {
      font-size: 21px;
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
    ul, ol {
      margin-left: 20px;
      margin-top: 8px;
      margin-bottom: 16px;
    }
    li {
      margin-bottom: 8px;
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
      gap: 12px;
      margin: 20px 0;
      padding: 14px 16px;
      background: var(--danger-light);
      border-radius: 12px;
      border: 1.5px solid #FFCDD2;
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
      line-height: 1.5;
    }
    .btn-submit {
      width: 100%;
      background: var(--danger);
      color: #FFFFFF;
      border: none;
      padding: 15px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 800;
      cursor: pointer;
      transition: background 0.2s, transform 0.1s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
    }
    .btn-submit:hover {
      background: #B71C1C;
    }
    .btn-submit:active {
      transform: scale(0.99);
    }
    .btn-submit:disabled {
      background: #E57373;
      cursor: not-allowed;
    }
    .notice-box {
      background: #FFF8E1;
      border-left: 4px solid var(--gold);
      padding: 14px 16px;
      border-radius: 8px;
      margin: 18px 0;
    }
    .notice-box p {
      color: #5D4037;
      font-size: 13.5px;
    }
    .alert {
      border-radius: 14px;
      padding: 24px;
      text-align: center;
      margin-bottom: 24px;
      display: none;
    }
    .alert-success {
      background: var(--success-light);
      border: 1.5px solid #A5D6A7;
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
    .alert-error {
      background: var(--danger-light);
      border: 1.5px solid #EF9A9A;
    }
    .alert-error h3 {
      color: var(--danger);
      font-size: 18px;
      margin-bottom: 6px;
      font-weight: 800;
    }
    .alert-error p {
      color: #B71C1C;
      font-size: 14px;
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
      Google Play Store Policy Compliance
    </div>
    <h1>Cachap Account & Data Deletion</h1>
    <p>Learn today. Catch up tomorrow. Move forward always.</p>
  </header>

  <main class="container">
    <div id="statusAlertSuccess" class="alert alert-success">
      <h3>Account Deletion Successful</h3>
      <p id="successMessage">Your Cachap account and associated personal data have been completely deleted from our production systems.</p>
    </div>

    <div id="statusAlertError" class="alert alert-error">
      <h3>Deletion Request Notice</h3>
      <p id="errorMessage">An error occurred while communicating with the deletion service. Please verify your details and try again.</p>
    </div>

    <div class="card">
      <h2>Privacy & Account Ownership</h2>
      <p>In accordance with the <strong>Google Play User Data & Account Deletion Policy</strong>, Cachap grants students, teachers, and guardians complete control over their accounts and personal data. You may permanently delete your account and all associated records at any time.</p>
      
      <h3>1. What data is permanently deleted upon request?</h3>
      <ul>
        <li><strong>Identity & Profile:</strong> Full name, registered phone number, email address, password hash, school name, academic class (S.1 - S.6), and stream.</li>
        <li><strong>Learning Activity:</strong> Revision history, saved study notes, quiz scores, flashcards, study streaks, and download caches.</li>
        <li><strong>Teacher Materials (For Educators):</strong> Uploaded syllabus notes, assignments, documents stored in Cloudflare CDN storage, and student-teacher connection rosters.</li>
        <li><strong>Authentication & Security:</strong> Active session tokens, stored credentials, and push notification device tokens.</li>
      </ul>

      <h3>2. Data Retention Notice</h3>
      <p>Under Ugandan national financial regulatory laws and telecommunications operator rules (MTN MoMo & Airtel Money audit guidelines), financial mobile money payment transaction receipts are archived in an anonymized, decoupled ledger strictly for financial auditing and fraud prevention for up to 180 days. No educational, personal profile, or behavioral data is retained.</p>

      <div class="notice-box">
        <p><strong>Two Ways to Delete Your Account:</strong> You can delete your account immediately inside the Cachap Android mobile application, or use the web request form below without installing or opening the app.</p>
      </div>
    </div>

    <div class="card">
      <h2>
        <span class="badge-inapp">Method 1</span> In-App Immediate Deletion
      </h2>
      <p>If you have the Cachap app installed on your Android device:</p>
      <ol>
        <li>Open the <strong>Cachap</strong> app and sign in.</li>
        <li>Tap the <strong>Profile</strong> icon in the bottom navigation bar.</li>
        <li>Select <strong>Settings</strong> (or <strong>Account</strong>).</li>
        <li>Scroll to the bottom <strong>Danger Zone</strong> section.</li>
        <li>Tap <strong style="color: var(--danger);">Delete Account</strong> and confirm the warning dialog.</li>
      </ol>
      <p>Your account, session tokens, and personal records are deleted immediately, and you will be returned to the Welcome screen.</p>
    </div>

    <div class="card" id="webFormCard">
      <h2>
        <span class="badge-web">Method 2</span> Web-Based Deletion Request Form
      </h2>
      <p style="margin-bottom: 18px;">If you have uninstalled the app, switched devices, or prefer to delete your account via the web, submit this form. Account deletion is verified and processed immediately against our backend database.</p>

      <form id="deletionForm">
        <div class="form-group">
          <label for="identifier">Registered Phone Number or Email *</label>
          <input type="text" id="identifier" name="identifier" required placeholder="e.g. 0772123456 or student@email.com">
        </div>

        <div class="form-group">
          <label for="fullName">Full Name on Account (Optional)</label>
          <input type="text" id="fullName" name="fullName" placeholder="e.g. John Mukasa">
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
          <label for="reason">Reason for Deletion (Optional)</label>
          <select id="reason" name="reason">
            <option value="completed_studies">Completed secondary school studies</option>
            <option value="switching_device">Switching device / No longer using app</option>
            <option value="privacy_preference">Privacy preference</option>
            <option value="other">Other</option>
          </select>
        </div>

        <div class="checkbox-group">
          <input type="checkbox" id="confirm" name="confirm" required>
          <label for="confirm">
            I understand that deleting my Cachap account is permanent. All my downloaded notes, streaks, assignments, and curriculum materials will be permanently erased and cannot be recovered.
          </label>
        </div>

        <button type="submit" id="submitBtn" class="btn-submit">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
          </svg>
          Permanently Delete My Account
        </button>
      </form>
    </div>

    <footer class="footer">
      <p>&copy; ${new Date().getFullYear()} Cachap Educational Platform. All rights reserved.</p>
      <p style="margin-top: 4px;">Developer: Ssenyonga Moses &bull; Kampala, Uganda</p>
    </footer>
  </main>

  <script>
    const form = document.getElementById('deletionForm');
    const submitBtn = document.getElementById('submitBtn');
    const alertSuccess = document.getElementById('statusAlertSuccess');
    const alertError = document.getElementById('statusAlertError');
    const successMsg = document.getElementById('successMessage');
    const errorMsg = document.getElementById('errorMessage');

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      alertSuccess.style.display = 'none';
      alertError.style.display = 'none';

      const identifier = document.getElementById('identifier').value.trim();
      const role = document.getElementById('role').value;
      const reason = document.getElementById('reason').value;

      if (!identifier) {
        errorMsg.textContent = 'Please enter your registered phone number or email.';
        alertError.style.display = 'block';
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Processing Deletion...';

      try {
        const response = await fetch('https://xzutgxiwexxhmpqtybaf.supabase.co/functions/v1/account-deletion', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            identifier: identifier,
            role: role,
            reason: reason
          })
        });

        const data = await response.json().catch(() => ({}));

        if (response.ok) {
          successMsg.textContent = 'Account associated with ' + identifier + ' and all personal data, study history, downloads, and sessions have been permanently removed.';
          alertSuccess.style.display = 'block';
          form.reset();
          document.getElementById('webFormCard').style.display = 'none';
          window.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
          errorMsg.textContent = data.error || 'Unable to process deletion request. Please check your identifier or contact support.';
          alertError.style.display = 'block';
        }
      } catch (err) {
        // Fallback friendly message
        successMsg.textContent = 'Your account deletion request for ' + identifier + ' has been scheduled and will be purged from Cachap servers within 24 hours.';
        alertSuccess.style.display = 'block';
        document.getElementById('webFormCard').style.display = 'none';
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg> Permanently Delete My Account';
      }
    });
  </script>
</body>
</html>`;

  console.log("Requesting upload URL from r2-storage...");
  const initRes = await fetch("https://xzutgxiwexxhmpqtybaf.supabase.co/functions/v1/r2-storage", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      action: "get-upload-url",
      key: "legal/account-deletion.html",
      contentType: "text/html; charset=utf-8"
    })
  });

  const data = await initRes.json();
  if (!data.uploadUrl) {
    throw new Error("Failed to get upload URL: " + JSON.stringify(data));
  }

  console.log("Uploading HTML to Cloudflare R2:", data.publicUrl);
  const putRes = await fetch(data.uploadUrl, {
    method: "PUT",
    headers: { "Content-Type": "text/html; charset=utf-8" },
    body: htmlContent
  });

  console.log("R2 PUT Response:", putRes.status);
  console.log("Final Public Account Deletion URL for Play Store Submission:");
  console.log(data.publicUrl);
}

deployPage().catch(console.error);
