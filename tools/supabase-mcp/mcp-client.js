#!/usr/bin/env node

/**
 * Supabase MCP Client Bridge for AI Studio Environment
 * 
 * Allows directly executing MCP requests against @supabase/mcp-server-supabase.
 * Usage:
 *   node ./tools/supabase-mcp/mcp-client.js list-tables
 *   node ./tools/supabase-mcp/mcp-client.js list-tools
 *   node ./tools/supabase-mcp/mcp-client.js call-tool <toolName> '<jsonArgs>'
 */

import { spawn } from "child_process";
import path from "path";
import { fileURLToPath } from "url";
import fs from "fs";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Project configuration
const PROJECT_REF = process.env.SUPABASE_PROJECT_REF || "xzutgxiwexxhmpqtybaf";
const READ_ONLY = process.env.SUPABASE_READ_ONLY === "true";

// Check for token from environment or optional local secret file
let token = process.env.SUPABASE_ACCESS_TOKEN;
const tokenFile = path.join(__dirname, ".supabase_pat");
if (!token && fs.existsSync(tokenFile)) {
  token = fs.readFileSync(tokenFile, "utf-8").trim();
}

const command = process.argv[2] || "list-tables";

if (!token) {
  console.log(JSON.stringify({
    status: "AWAITING_TOKEN",
    message: "SUPABASE_ACCESS_TOKEN is not yet set. Please provide your Supabase Personal Access Token (PAT).",
    project_ref: PROJECT_REF,
    read_only: READ_ONLY
  }, null, 2));
  process.exit(0);
}

const cliPath = path.join(__dirname, "node_modules/@supabase/mcp-server-supabase/dist/cli.js");

const args = [
  cliPath,
  "--access-token", token,
  "--project-ref", PROJECT_REF
];

if (READ_ONLY) {
  args.push("--read-only");
}

const child = spawn("node", args, {
  stdio: ["pipe", "pipe", "pipe"],
  env: { ...process.env, SUPABASE_ACCESS_TOKEN: token }
});

let buffer = "";
const responses = new Map();
let nextId = 1;

function sendRpc(method, params = {}) {
  const id = nextId++;
  const payload = JSON.stringify({
    jsonrpc: "2.0",
    id,
    method,
    params
  });
  return new Promise((resolve, reject) => {
    responses.set(id, { resolve, reject });
    child.stdin.write(payload + "\n");
  });
}

child.stdout.on("data", (data) => {
  buffer += data.toString();
  const lines = buffer.split("\n");
  buffer = lines.pop(); // keep remainder

  for (const line of lines) {
    if (!line.trim()) continue;
    try {
      const msg = JSON.parse(line.trim());
      if (msg.id && responses.has(msg.id)) {
        const { resolve } = responses.get(msg.id);
        responses.delete(msg.id);
        resolve(msg);
      }
    } catch (e) {
      // not JSON line or log line
    }
  }
});

child.stderr.on("data", (data) => {
  const errStr = data.toString();
  if (process.env.DEBUG) {
    console.error("[MCP STDERR]", errStr);
  }
});

async function main() {
  try {
    // 1. Initialize MCP handshake
    const initRes = await sendRpc("initialize", {
      protocolVersion: "2024-11-05",
      capabilities: {},
      clientInfo: { name: "ai-studio-bridge", version: "1.0.0" }
    });

    // 2. Initialized notification
    child.stdin.write(JSON.stringify({
      jsonrpc: "2.0",
      method: "notifications/initialized"
    }) + "\n");

    if (command === "list-tools") {
      const toolsRes = await sendRpc("tools/list", {});
      console.log(JSON.stringify(toolsRes.result, null, 2));
    } else if (command === "list-tables") {
      const callRes = await sendRpc("tools/call", {
        name: "list_tables",
        arguments: { project_ref: PROJECT_REF }
      });
      console.log(JSON.stringify(callRes.result || callRes, null, 2));
    } else if (command === "call-tool") {
      const toolName = process.argv[3];
      const toolArgs = process.argv[4] ? JSON.parse(process.argv[4]) : {};
      const callRes = await sendRpc("tools/call", {
        name: toolName,
        arguments: toolArgs
      });
      console.log(JSON.stringify(callRes.result || callRes, null, 2));
    } else {
      console.log(`Unknown command: ${command}`);
    }
  } catch (err) {
    console.error("MCP Execution error:", err);
  } finally {
    child.kill();
    process.exit(0);
  }
}

main();
