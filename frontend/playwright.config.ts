import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 90_000,
  expect: { timeout: 15_000 },
  workers: 1,
  retries: 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:4300',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: ['chrome', 'firefox'].flatMap((browser) =>
    [360, 768, 1366].map((width) => ({
      name: `${browser}-${width}`,
      use: {
        browserName: browser === 'chrome' ? ('chromium' as const) : ('firefox' as const),
        ...(browser === 'chrome' ? { channel: 'chrome' } : {}),
        viewport: { width, height: 900 },
      },
    })),
  ),
  webServer: [
    {
      command: 'powershell -NoProfile -File ../scripts/Invoke-Backend.ps1 -Task Run -Port 18080',
      url: 'http://localhost:18080/api/v1/health',
      timeout: 180_000,
      reuseExistingServer: false,
    },
    {
      command: 'npm.cmd start -- --port 4300 --host localhost --proxy-config proxy.e2e.conf.json',
      url: 'http://localhost:4300',
      timeout: 120_000,
      reuseExistingServer: false,
    },
  ],
});
