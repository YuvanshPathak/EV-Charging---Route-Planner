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
 * RegisterTest — negative tests for the ZapGo registration page (/register).
 *
 * Selectors derived from src/pages/Register.jsx:
 *   Name input            → input[name='name']            placeholder="Full Name"
 *   Email input           → input[name='email']           placeholder="Email Address"
 *   Password input        → input[name='password']        placeholder="Password"
 *   Confirm Pwd input     → input[name='confirmPassword'] placeholder="Confirm Password"
 *   Register button       → button.primary-btn  (text "Register", type=submit)
 *   Error message         → div.error-msg
 *   Page heading          → h1.title  (text "Create Account")
 *
 * We do NOT test a successful new registration to avoid creating Firebase records.
 */
public class RegisterTest extends BaseTest {

    private static final Duration WAIT = Duration.ofSeconds(10);

    // -----------------------------------------------------------------------
    // @BeforeClass – navigate to the registration page before tests
    // -----------------------------------------------------------------------

    @BeforeClass
    public void goToRegisterPage() {
        navigateTo("/register");
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Test 1: Registration page loads with all four form fields visible.
     */
    @Test(priority = 1)
    public void testRegisterPageLoads() {
        navigateTo("/register");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // Heading "Create Account"
        WebElement heading = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title"))
        );
        Assert.assertTrue(heading.getText().contains("Create Account"),
                "Register page heading should say 'Create Account'");

        // Name field (name="name" in JSX)
        WebElement nameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='name']"))
        );
        Assert.assertTrue(nameField.isDisplayed(), "Full Name input should be visible");

        // Email field
        WebElement emailField = driver.findElement(By.cssSelector("input[name='email']"));
        Assert.assertTrue(emailField.isDisplayed(), "Email input should be visible");

        // Password field
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        Assert.assertTrue(passwordField.isDisplayed(), "Password input should be visible");

        // Confirm password field
        WebElement confirmField = driver.findElement(By.cssSelector("input[name='confirmPassword']"));
        Assert.assertTrue(confirmField.isDisplayed(), "Confirm Password input should be visible");
    }

    /**
     * Test 2: Mismatched passwords show "Passwords do not match" error.
     * From Register.jsx line 43-45: setError("Passwords do not match");
     */
    @Test(priority = 2)
    public void testPasswordMismatchShowsError() {
        navigateTo("/register");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // Wait for form to be ready
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='name']")));

        // Fill name (letters only to pass nameRegex)
        driver.findElement(By.cssSelector("input[name='name']")).sendKeys("Test User");
        driver.findElement(By.cssSelector("input[name='email']")).sendKeys("mismatch@test.com");
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys("Password1234");
        driver.findElement(By.cssSelector("input[name='confirmPassword']")).sendKeys("DifferentPass");

        // Submit
        driver.findElement(By.cssSelector("button.primary-btn")).click();

        // Error div should appear with password mismatch message
        WebElement errorDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.error-msg"))
        );
        Assert.assertTrue(errorDiv.isDisplayed(), "Error message should be visible");
        Assert.assertTrue(errorDiv.getText().toLowerCase().contains("match"),
                "Error should mention passwords do not match, got: " + errorDiv.getText());
    }

    /**
     * Test 3: Registering with an already-existing email shows Firebase error.
     * From Register.jsx line 79: "An account with this email already exists. Please login instead."
     */
    @Test(priority = 3)
    public void testRegisterWithExistingEmail() {
        navigateTo("/register");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='name']")));

        driver.findElement(By.cssSelector("input[name='name']")).sendKeys("ZapGo Tester");
        driver.findElement(By.cssSelector("input[name='email']")).sendKeys("testuser@zapgo.com");
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys("Test@1234");
        driver.findElement(By.cssSelector("input[name='confirmPassword']")).sendKeys("Test@1234");

        driver.findElement(By.cssSelector("button.primary-btn")).click();

        // Firebase will return auth/email-already-in-use → div.error-msg
        // Allow extra time for the Firebase async call
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement errorDiv = longWait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.error-msg"))
        );
        Assert.assertTrue(errorDiv.isDisplayed(),
                "An error message should appear for a duplicate email");

        String errorText = errorDiv.getText().toLowerCase();
        // The message contains "already" (from Register.jsx: "already exists")
        Assert.assertTrue(errorText.contains("already") || errorText.contains("email"),
                "Error should mention email already exists, got: " + errorDiv.getText());
    }
}
