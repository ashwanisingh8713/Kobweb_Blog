// A simple Puppeteer script to test the /admin/create page
// Usage: node test_create_page.js

const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

function findChromeExecutable() {
  // Common macOS/Chromium/Chrome locations
  const candidates = [
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    '/Applications/Chromium.app/Contents/MacOS/Chromium',
    '/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary'
  ];
  for (const c of candidates) {
    if (fs.existsSync(c)) return c;
  }
  return null;
}

(async () => {
  const TARGET_URL = process.env.TARGET_URL || 'http://localhost:8080/admin/create';
  console.log('Target URL:', TARGET_URL);

  const chromePath = findChromeExecutable();
  if (chromePath) console.log('Found Chrome at', chromePath);

  const launchOptions = {
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  };
  if (chromePath) launchOptions.executablePath = chromePath;

  const browser = await puppeteer.launch(launchOptions);
  const page = await browser.newPage();

  // Ensure the app sees a logged-in user before any app JS runs.
  // This prevents the SPA from redirecting to the login page.
  await page.evaluateOnNewDocument(() => {
    try {
      localStorage.setItem('username', 'puppeteer-tester');
    } catch (e) {
      // ignore
    }
  });

  page.on('console', msg => console.log('PAGE LOG:', msg.text()));
  page.on('pageerror', err => console.error('PAGE ERROR:', err.toString()));

  try {
    await page.goto(TARGET_URL, { waitUntil: 'networkidle2', timeout: 60000 });
    console.log('Page loaded, waiting for inputs...');

    // wait for inputs to appear
    // give the SPA more time to mount in CI-like environments
    await page.waitForSelector('#titleInput', { timeout: 60000 });
    await page.waitForSelector('#subtitleInput', { timeout: 60000 });
    await page.waitForSelector('#thumbnailInput', { timeout: 60000 });
    await page.waitForSelector('#editor', { timeout: 60000 });

    // fill fields
    await page.focus('#titleInput');
    await page.keyboard.type('E2E Test Title');
    await page.focus('#subtitleInput');
    await page.keyboard.type('E2E Test Subtitle');
    await page.focus('#thumbnailInput');
    await page.keyboard.type('https://via.placeholder.com/400x200');
    await page.focus('#editor');
    await page.keyboard.type('This is the test content from puppeteer.');

    // click create button (the visible inline button or the sticky one)
    const createSelector = 'button';
    // find a button with innerText containing Create
    const createButton = await page.$$eval('button', btns => {
      const b = btns.find(x => x.innerText && x.innerText.trim().toLowerCase().includes('create'));
      return b ? b.outerHTML : null;
    });

    if (createButton) {
      console.log('Create button found, clicking...');
      await page.$$eval('button', btns => {
        const b = btns.find(x => x.innerText && x.innerText.trim().toLowerCase().includes('create'));
        if (b) b.click();
      });
    } else {
      console.log('Create button not found');
    }

    // wait some time for navigation or popup
    await page.waitForTimeout(5000);

    // capture some DOM state
    const state = await page.evaluate(() => {
      return {
        title: document.getElementById('titleInput')?.value || null,
        subtitle: document.getElementById('subtitleInput')?.value || null,
        thumbnail: document.getElementById('thumbnailInput')?.value || null,
        editor: (document.getElementById('editor') instanceof HTMLTextAreaElement)
          ? document.getElementById('editor').value
          : document.getElementById('editor')?.innerText || null,
        url: location.href
      };
    });

    console.log('Captured state after click:', state);
  } catch (e) {
    console.error('Test script error:', e);
  } finally {
    await browser.close();
  }
})();
