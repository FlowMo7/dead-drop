import { test, before, after } from "node:test";
import assert from "node:assert/strict";
import puppeteer from "puppeteer";

const BASE_URL = process.env.E2E_BASE_URL ?? "http://localhost:8080";
const LAUNCH_ARGS = {
  headless: true,
  args: ["--no-sandbox", "--disable-setuid-sandbox"],
};

let browser;
let page;

before(async () => {
  browser = await puppeteer.launch(LAUNCH_ARGS);
  await waitForAppReady();
});

after(async () => {
  if (browser) {
    await browser.close();
  }
});

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

async function newPage() {
  page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });
  return page;
}

async function createDrop(page, message) {
  await page.goto(`${BASE_URL}/`, { waitUntil: "networkidle0" });
  await page.type("#drop_content", message);
  await page.evaluate(() => sendDrop(document.getElementById("drop_content").value));
  await page.waitForSelector("#link_div", { visible: true });
  const pickupUrl = await page.$eval("#drop_share_link", (el) => el.textContent);
  const password = await page.$eval("#drop_share_password", (el) => el.textContent);
  return { pickupUrl: pickupUrl.trim(), password: password.trim() };
}

async function pickupDrop(page, pickupUrl, password) {
  await page.goto(pickupUrl, { waitUntil: "networkidle0" });
  await page.type("#drop_password", password);
  await page.evaluate((id) => getDrop(id, document.getElementById("drop_password").value), new URL(pickupUrl).pathname.split("/").pop());
  await page.waitForSelector("#drop_content_section", { visible: true });
  return page.$eval("#drop_content", (el) => el.textContent);
}

test("home page loads", async () => {
  await newPage();
  await page.goto(`${BASE_URL}/`, { waitUntil: "networkidle0" });
  await page.waitForSelector("#send_div", { visible: true });
  assert.ok(await page.$("#drop_content"));
});

test("full flow: create drop, then pick it up", async () => {
  const p = await newPage();
  const message = `Top secret message ${Date.now()}`;
  const { pickupUrl, password } = await createDrop(p, message);

  assert.ok(/^https?:\/\/.+/.test(pickupUrl), `pickupUrl should be an absolute URL: ${pickupUrl}`);
  assert.ok(password.length >= 16, "password should be generated (16 chars)");

  const decrypted = await pickupDrop(p, pickupUrl, password);
  assert.equal(decrypted, message, "decrypted pickup content should match the original message");
});

test("pick up twice: create drop, then pick it up, second pick up shows error", async () => {
  const p = await newPage();
  const message = `Top secret message ${Date.now()}`;
  const { pickupUrl, password } = await createDrop(p, message);

  assert.ok(/^https?:\/\/.+/.test(pickupUrl), `pickupUrl should be an absolute URL: ${pickupUrl}`);
  assert.ok(password.length >= 16, "password should be generated (16 chars)");

  const decrypted = await pickupDrop(p, pickupUrl, password);
  assert.equal(decrypted, message, "decrypted pickup content should match the original message");

  await p.goto(pickupUrl, { waitUntil: "networkidle0" });
  await p.type("#drop_password", password);
  await p.evaluate((id) => getDrop(id, document.getElementById("drop_password").value), new URL(pickupUrl).pathname.split("/").pop());
  await p.waitForSelector("#error_text", { visible: true });
  assert.ok(await p.$("#error_text"));

});

test("picking up with a wrong password shows an error", async () => {
  const p = await newPage();
  const { pickupUrl } = await createDrop(p, "message that must not be recovered with a wrong password");

  await p.goto(pickupUrl, { waitUntil: "networkidle0" });
  await p.type("#drop_password", "definitely-wrong-password");
  await p.evaluate((id) => getDrop(id, document.getElementById("drop_password").value), new URL(pickupUrl).pathname.split("/").pop());
  await p.waitForSelector("#error_text", { visible: true });
  assert.ok(await p.$("#error_text"));
});
