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
 * BookingsPageTest — tests for the bookings page (/app/bookings).
 *
 * Selectors derived from src/pages/BookingsPage.jsx:
 *
 *   Page heading          → div.bookings-header  → h1 (text "Your Bookings")
 *   Stats section         → div.bookings-stats
 *   Stat cards            → div.stat-card
 *   Stat labels           → p.stat-label  (texts: "Total Trips", "Total Distance", "Total Time")
 *   Bookings table        → table.bookings-table  (present when bookings > 0)
 *   Empty state message   → p.bookings-empty-state  (present when no bookings)
 *   Table wrapper         → div.bookings-table-wrap
 *
 * @BeforeClass logs in and navigates to /app/bookings.
 */
public class BookingsPageTest extends BaseTest {

    private static final String EMAIL    = "yuvansh.bookings@gmail.com";
    private static final String PASSWORD = "123456";
    private static final Duration WAIT   = Duration.ofSeconds(15);

    // -----------------------------------------------------------------------
    // @BeforeClass – log in and open the bookings page
    // -----------------------------------------------------------------------

    @BeforeClass
    public void loginAndOpenBookingsPage() {
        navigateTo("/login");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement emailInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[placeholder='Email Address']"))
        );
        emailInput.clear();
        emailInput.sendKeys(EMAIL);

        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button.primary-btn")).click();

        wait.until(ExpectedConditions.urlContains("/app"));
        navigateTo("/app/bookings");
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Test 1: Bookings page loads with the "Your Bookings" heading visible.
     * BookingsPage.jsx line 102-104:
     *   <div className="bookings-header">
     *     <h1>Your Bookings</h1>
     */
    @Test(priority = 1)
    public void testBookingsPageLoads() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement header = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.bookings-header"))
        );
        Assert.assertTrue(header.isDisplayed(), "Bookings header should be visible");

        WebElement h1 = header.findElement(By.tagName("h1"));
        Assert.assertEquals(h1.getText().trim(), "Your Bookings",
                "Bookings page h1 should say 'Your Bookings'");
    }

    /**
     * Test 2: Either a bookings table OR the empty-state message is present.
     *
     * BookingsPage.jsx:
     *   line 138: <p className="bookings-empty-state">You don't have any bookings yet...</p>
     *   line 145: <table className="bookings-table">
     *
     * Both are valid states depending on account history.
     */
    @Test(priority = 2)
    public void testBookingsTableOrEmptyState() {
        // Wait a little for Firestore to load
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // First wait for the loading spinner/badge to disappear
        // (bookings-header contains a "Loading..." badge while data is fetching)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("span.badge-muted")
        ));

        // Now check: either the table or the empty-state paragraph must be in the DOM
        boolean tablePresent = !driver.findElements(
                By.cssSelector("table.bookings-table")).isEmpty();
        boolean emptyStatePresent = !driver.findElements(
                By.cssSelector("p.bookings-empty-state")).isEmpty();

        Assert.assertTrue(tablePresent || emptyStatePresent,
                "Either a bookings table or an empty-state message must be present");
    }

    /**
     * Test 3: The stats section with three stat-cards is visible.
     *
     * BookingsPage.jsx lines 116-133:
     *   <div className="bookings-stats">
     *     <div className="stat-card">  <!-- Total Trips -->
     *     <div className="stat-card">  <!-- Total Distance -->
     *     <div className="stat-card">  <!-- Total Time -->
     *
     * Stats are always rendered regardless of whether bookings exist.
     */
    @Test(priority = 3)
    public void testStatsAreVisible() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement statsSection = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.bookings-stats"))
        );
        Assert.assertTrue(statsSection.isDisplayed(), "Stats section should be visible");

        // Three stat-cards must be present
        java.util.List<WebElement> statCards = statsSection.findElements(
                By.cssSelector("div.stat-card"));
        Assert.assertEquals(statCards.size(), 3,
                "There should be exactly 3 stat cards (Total Trips, Total Distance, Total Time)");

        // Verify labels exist (Total Trips, Total Distance, Total Time)
        boolean hasTotalTrips     = statCards.stream().anyMatch(c -> c.getText().contains("TOTAL TRIPS"));
        boolean hasTotalDistance  = statCards.stream().anyMatch(c -> c.getText().contains("TOTAL DISTANCE"));
        boolean hasTotalTime      = statCards.stream().anyMatch(c -> c.getText().contains("TOTAL TIME"));

        Assert.assertTrue(hasTotalTrips,    "Stats should include 'Total Trips'");
        Assert.assertTrue(hasTotalDistance, "Stats should include 'Total Distance'");
        Assert.assertTrue(hasTotalTime,     "Stats should include 'Total Time'");
    }
}
