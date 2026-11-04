import { mkdir } from "node:fs/promises";
import path from "node:path";
import puppeteer from "puppeteer";

const BASE_URL = process.env.E2E_BASE_URL ?? "http://localhost:8080";
const OUT_DIR = process.env.SCREENSHOT_OUT_DIR ?? "/screenshots";

// All languages supported by the application (see Localization.Language).
const ALL_LANGUAGES = ["en", "de", "es", "cs", "fr", "tr", "it", "nl", "ro", "uk", "pl"];

// Languages are parameterized via SCREENSHOT_LANGUAGES (comma-separated),
// defaulting to every supported language.
const LANGUAGES = (process.env.SCREENSHOT_LANGUAGES ?? ALL_LANGUAGES.join(","))
  .split(",")
  .map((lang) => lang.trim())
  .filter((lang) => lang.length > 0);

const VIEWPORT = { width: 1280, height: 800 };

let browser;

async function waitForAppReady() {
  const p = await browser.newPage();
  for (let attempt = 0; attempt < 30; attempt++) {
    try {
      const res = await p.goto(`${BASE_URL}/status`, { waitUntil: "domcontentloaded", timeout: 2000 });
      const text = await res.text();
      if (text === "Ok") {
        await p.close();
        return;
      }
    } catch {
      // app not ready yet
    }
    await new Promise((r) => setTimeout(r, 1000));
  }
  await p.close().catch(() => {});
  throw new Error(`App at ${BASE_URL} did not become ready within timeout`);
}

async function newPage(language) {
  const page = await browser.newPage();
  await page.setViewport(VIEWPORT);
  await page.setExtraHTTPHeaders({ "Accept-Language": language });
  return page;
}

function withLanguage(url, language) {
  const parsed = new URL(url);
  parsed.searchParams.set("hl", language);
  return parsed.toString();
}

async function screenshot(page, language, name) {
  const file = path.join(OUT_DIR, `${language}-${name}.png`);
  await page.screenshot({ path: file, fullPage: true });
  console.log(`  saved ${file}`);
}

async function captureLanguage(language) {
  console.log(`Capturing screenshots for language '${language}'`);
  const page = await newPage(language);
  try {
    // 1. Home / create-a-drop page
    await page.goto(withLanguage(`${BASE_URL}/`, language), { waitUntil: "networkidle0" });
    await page.waitForSelector("#send_div", { visible: true });
    await screenshot(page, language, "home");

    // Create a real drop so we can screenshot the pickup page.
    const message = `Screenshot sample secret (${language})`;
    await page.type("#drop_content", message);
    await page.evaluate(() => sendDrop(document.getElementById("drop_content").value));
    await page.waitForSelector("#link_div", { visible: true });
    const pickupUrl = (await page.$eval("#drop_share_link", (el) => el.textContent)).trim();
    const password = (await page.$eval("#drop_share_password", (el) => el.textContent)).trim();

    // 2. Pickup page: password-entry state
    const localizedPickupUrl = withLanguage(pickupUrl, language);
    await page.goto(localizedPickupUrl, { waitUntil: "networkidle0" });
    await page.waitForSelector("#drop_password", { visible: true });
    await screenshot(page, language, "pickup");

    // 3. Pickup page: decrypted-content state
    await page.type("#drop_password", password);
    await page.evaluate(
      (id) => getDrop(id, document.getElementById("drop_password").value),
      new URL(pickupUrl).pathname.split("/").pop(),
    );
    await page.waitForSelector("#drop_content_section", { visible: true });
    await screenshot(page, language, "pickup-content");
  } finally {
    await page.close().catch(() => {});
  }
}

async function main() {
  await mkdir(OUT_DIR, { recursive: true });
  browser = await puppeteer.launch({
    headless: true,
    args: ["--no-sandbox", "--disable-setuid-sandbox"],
  });

  try {
    await waitForAppReady();

    const failures = [];
    for (const language of LANGUAGES) {
      try {
        await captureLanguage(language);
      } catch (error) {
        console.error(`  FAILED for language '${language}': ${error.message}`);
        failures.push(language);
      }
    }

    if (failures.length > 0) {
      throw new Error(`Screenshot capture failed for: ${failures.join(", ")}`);
    }
    console.log(`Done. Screenshots for ${LANGUAGES.length} language(s) written to ${OUT_DIR}`);
  } finally {
    if (browser) {
      await browser.close();
    }
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
