import {expect, test} from '@playwright/test';

test.describe('Main flow: Create vision and confirm pre-arrest', () => {
    test.beforeEach(async ({page}) => {
        await page.goto('/login');
        await page.fill('input[placeholder="USERNAME..."]', 'precog');
        await page.fill('input[placeholder="PASSWORD..."]', 'agatha');
        await page.click('button:has-text("AUTHORIZE")');
        await page.waitForURL('/', {timeout: 10000});
        await expect(page.getByText('Department of Pre-Crime')).toBeVisible();
    });

    test('should create a vision, see pending pre-arrest, and confirm it', async ({page}) => {
        const firstName = 'Jane';
        const lastName = 'Smith';

        await page.fill('input[placeholder="First Name..."]', firstName);
        await page.fill('input[placeholder="Last Name..."]', lastName);
        await page.selectOption('select[name="crimeType"]', 'Murder');
        await page.click('button:has-text("Submit to Precogs")');

        await expect(page.getByText(`${firstName} ${lastName}`).first()).toBeVisible({timeout: 15000});

        await expect(page.getByText('PENDING').first()).toBeVisible({timeout: 15000});

        await page.click('button:has-text("Confirm Pre-Arrest executed")');

        await expect(page.getByText('No pending Pre-Arrests...')).toBeVisible({timeout: 10000});

        await expect(page.getByText('ARRESTED_BEFORE_CRIME').first()).toBeVisible({timeout: 15000});

        const statsText = await page.locator('.stats-number').textContent();
        const statsValue = parseInt(statsText!.trim(), 10);
        expect(statsValue).toBeGreaterThan(0);
    });

    test('should show vision and arrest in audit log', async ({page}) => {
        await page.fill('input[placeholder="First Name..."]', 'Alice');
        await page.fill('input[placeholder="Last Name..."]', 'Wonder');
        await page.selectOption('select[name="crimeType"]', 'Theft');
        await page.click('button:has-text("Submit to Precogs")');

        await expect(page.getByText('Alice Wonder').first()).toBeVisible({timeout: 15000});
        await page.click('button:has-text("Confirm Pre-Arrest executed")');

        await expect(page.locator('.log-feed')).toContainText('CrimeForeseenEvent', {timeout: 15000});
        await expect(page.locator('.log-feed')).toContainText('PreArrestExecutedEvent', {timeout: 15000});
        await expect(page.locator('.log-feed')).toContainText('PreApologyIssuedEvent', {timeout: 15000});
    });
});
