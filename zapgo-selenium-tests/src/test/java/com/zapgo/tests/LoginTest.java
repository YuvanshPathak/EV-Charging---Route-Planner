package com.zapgo.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * LoginTest — tests for the ZapGo login page (/login).
 *
 * Selectors derived from src/pages/Login.jsx:
 *   Email input       → input[placeholder='Email Address']  (class="input-field", type=email)
 *   Password input    → input[placeholder='Password']        (class="input-field", type=password)
 *   Login button      → button.primary-btn  (text "Login", type=submit)
 *   Error message     → div.error-msg
 *   Logout button     → button.topbar-logout  (Topbar.jsx)
 *   Page heading      → h1.title  (contains "ZapGo")
 */
public class LoginTest extends BaseTest {

    private static final String EMAIL       = "testuser@zapgo.com";
    private static final String PASSWORD    = "Test@1234";
    private static final Duration WAIT      = Duration.ofSeconds(10);

    // -----------------------------------------------------------------------
    // @BeforeClass – start every test at the login page
    // -----------------------------------------------------------------------

    @BeforeClass
    public void goToLoginPage() {
        navigateTo("/login");
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Test 1: Login page loads correctly.
     * Asserts the ZapGo heading is visible and the email field is present.
     */
    @Test(priority = 1)
    public void testLoginPageLoads() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // h1.title contains "ZapGo"
        WebElement heading = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title"))
        );
        Assert.assertTrue(heading.getText().contains("ZapGo"),
                "Page heading should contain 'ZapGo'");

        // Email input visible
        WebElement emailInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[placeholder='Email Address']"))
        );
        Assert.assertTrue(emailInput.isDisplayed(), "Email input should be visible");
    }

    /**
     * Test 2: Invalid credentials show an error message.
     * Error element: div.error-msg
     */
    @Test(priority = 2)
    public void testInvalidLoginShowsError() {
        navigateTo("/login");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // Fill in wrong credentials
        WebElement emailInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[placeholder='Email Address']"))
        );
        emailInput.clear();
        emailInput.sendKeys("wrong@email.com");

        WebElement passwordInput = driver.findElement(
                By.cssSelector("input[placeholder='Password']"));
        passwordInput.clear();
        passwordInput.sendKeys("wrongpass");

        // Click the Login (submit) button
        driver.findElement(By.cssSelector("button.primary-btn")).click();

        // Wait for error message div to appear
        WebElement errorDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.error-msg"))
        );
        Assert.assertTrue(errorDiv.isDisplayed(),
                "An error message should be displayed for invalid credentials");
        Assert.assertFalse(errorDiv.getText().isEmpty(),
                "Error message text should not be empty");
    }

    /**
     * Test 3: Valid credentials redirect to the app (URL contains /app).
     */
    @Test(priority = 3)
    public void testValidLogin() {
        navigateTo("/login");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement emailInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[placeholder='Email Address']"))
        );
        emailInput.clear();
        emailInput.sendKeys(EMAIL);

        WebElement passwordInput = driver.findElement(
                By.cssSelector("input[placeholder='Password']"));
        passwordInput.clear();
        passwordInput.sendKeys(PASSWORD);

        driver.findElement(By.cssSelector("button.primary-btn")).click();

        // After successful login React Router redirects to /app
        wait.until(ExpectedConditions.urlContains("/app"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/app"),
                "After valid login URL should contain '/app'");
    }

    /**
     * Test 4: Logout button in the Topbar redirects back to /login.
     * Selector from Topbar.jsx: button.topbar-logout
     */
    @Test(priority = 4)
    public void testLogout() {
        // Ensure we are logged in and on the app page
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/app"));

        // Find and click the Topbar logout button (button.topbar-logout)
        WebElement logoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.topbar-logout"))
        );
        logoutBtn.click();

        // Should redirect back to /login
        wait.until(ExpectedConditions.urlContains("/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "After logout URL should contain '/login'");
    }
}
