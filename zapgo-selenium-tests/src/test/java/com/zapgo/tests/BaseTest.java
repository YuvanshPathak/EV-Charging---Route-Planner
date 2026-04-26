package com.zapgo.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * BaseTest — manages a single shared WebDriver instance for the entire test suite.
 * All test classes extend this class.
 *
 * Driver lifecycle:
 *   @BeforeSuite  → setup ChromeDriver + launch Chrome (maximised, visible)
 *   @AfterSuite   → quit browser
 *
 * Helper:
 *   navigateTo(String path) → opens http://localhost:5173{path}
 */
public class BaseTest {

    /** Shared driver instance across the whole suite (sequential execution). */
    protected static WebDriver driver;

    private static final String BASE_URL = "http://localhost:5173";

    // -----------------------------------------------------------------------
    // Suite-level driver initialisation
    // -----------------------------------------------------------------------

    @BeforeSuite
    public void setUpSuite() {
        // WebDriverManager resolves and downloads the matching ChromeDriver binary
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        // NOTE: intentionally NOT headless so the evaluator can watch the run

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(
                java.time.Duration.ofSeconds(0) // use explicit waits everywhere
        );
    }

    @AfterSuite
    public void tearDownSuite() {
        if (driver != null) {
            driver.quit();
        }
    }

    // -----------------------------------------------------------------------
    // Navigation helper
    // -----------------------------------------------------------------------

    /**
     * Navigate to BASE_URL + path.
     * Examples:
     *   navigateTo("/login")           → http://localhost:5173/login
     *   navigateTo("/app")             → http://localhost:5173/app
     *   navigateTo("/admin/dashboard") → http://localhost:5173/admin/dashboard
     */
    protected void navigateTo(String path) {
        driver.get(BASE_URL + path);
    }
}
