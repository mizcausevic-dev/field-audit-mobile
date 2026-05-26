import { readFileSync, writeFileSync, readdirSync, existsSync, statSync } from "node:fs";
import { join, relative } from "node:path";

const domain = process.argv[2] || "";
if (!domain || !existsSync("site")) {
  process.exit(0);
}

const pkg = JSON.parse(readFileSync("package.json", "utf8"));
const desc = String(pkg.description || "").replace(/\s+/g, " ").trim();

const esc = (value) =>
  String(value)
    .replace(/&/g, "&amp;")
    .replace(/"/g, "&quot;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");

function listHtmlFiles(dir) {
  const entries = [];
  for (const name of readdirSync(dir)) {
    const full = join(dir, name);
    const stat = statSync(full);
    if (stat.isDirectory()) {
      entries.push(...listHtmlFiles(full));
    } else if (name.endsWith(".html")) {
      entries.push(full);
    }
  }
  return entries;
}

for (const path of listHtmlFiles("site")) {
  let html = readFileSync(path, "utf8");
  if (html.includes('property="og:title"')) continue;

  const match = html.match(/<title>([^<]*)<\/title>/);
  const title = match ? match[1].trim() : domain;
  const rel = relative("site", path).replace(/\\/g, "/");
  const page = rel === "index.html" ? "" : rel.replace(/index\.html$/, "");
  const url = `https://${domain}/${page}`;
  const tags = [
    `<link rel="canonical" href="${url}">`,
    `<meta name="description" content="${esc(desc)}">`,
    `<meta property="og:type" content="website">`,
    `<meta property="og:title" content="${esc(title)}">`,
    `<meta property="og:description" content="${esc(desc)}">`,
    `<meta property="og:url" content="${url}">`,
    `<meta property="og:site_name" content="Kinetic Gain">`,
    `<meta name="twitter:card" content="summary">`,
    `<meta name="twitter:title" content="${esc(title)}">`,
    `<meta name="twitter:description" content="${esc(desc)}">`
  ].join("\n  ");
  html = html.replace("</head>", `  ${tags}\n</head>`);
  writeFileSync(path, html);
}
