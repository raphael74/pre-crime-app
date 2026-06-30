import {expect, test} from '@playwright/test';

test.describe('Main flow: Create vision and confirm pre-arrest', () => {
    test.beforeEach(async ({page}) => {
        await page.goto('/login');
        await page.fill('[data-testid="username-input"]', 'precog');
        await page.fill('[data-testid="password-input"]', 'agatha');
        await page.click('[data-testid="authorize-btn"]');
        await page.waitForURL('/', {timeout: 10000});
        await expect(page.getByText('Department of Pre-Crime')).toBeVisible();
    });

    test('should create a vision, see pending pre-arrest, and confirm it', async ({page}) => {
        const firstName = 'Jane';
        const lastName = 'Smith';

        await page.fill('[data-testid="first-name-input"]', firstName);
        await page.fill('[data-testid="last-name-input"]', lastName);
        await page.selectOption('[data-testid="crime-type-select"]', 'Murder');
        await page.click('[data-testid="submit-vision-btn"]');

        await expect(page.getByText(`${firstName} ${lastName}`).first()).toBeVisible({timeout: 15000});

        await expect(page.getByText('PENDING').first()).toBeVisible({timeout: 15000});

        await page.click('[data-testid="confirm-pre-arrest-btn"]');

        await expect(page.getByText('No pending Pre-Arrests...')).toBeVisible({timeout: 10000});

        await expect(page.getByText('ARRESTED_BEFORE_CRIME').first()).toBeVisible({timeout: 15000});

        const statsText = await page.locator('.stats-number').textContent();
        const statsValue = parseInt(statsText!.trim(), 10);
        expect(statsValue).toBeGreaterThan(0);
    });

    test('should show vision and arrest in audit log', async ({page}) => {
        await page.fill('[data-testid="first-name-input"]', 'Alice');
        await page.fill('[data-testid="last-name-input"]', 'Wonder');
        await page.selectOption('[data-testid="crime-type-select"]', 'Theft');
        await page.click('[data-testid="submit-vision-btn"]');

        await expect(page.getByText('Alice Wonder').first()).toBeVisible({timeout: 15000});
        await page.click('[data-testid="confirm-pre-arrest-btn"]');

        await expect(page.locator('.log-feed')).toContainText('CrimeForeseenEvent', {timeout: 15000});
        await expect(page.locator('.log-feed')).toContainText('PreArrestExecutedEvent', {timeout: 15000});
        await expect(page.locator('.log-feed')).toContainText('PreApologyIssuedEvent', {timeout: 15000});
    });
});
