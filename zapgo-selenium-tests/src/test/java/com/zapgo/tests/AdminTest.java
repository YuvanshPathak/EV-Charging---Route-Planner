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
 * AdminTest — tests for the Admin Login and Dashboard pages.
 *
 * Selectors derived from src/pages/AdminLogin.jsx:
 *   Username input    → input#username           (id="username")
 *   Password input    → input#password           (id="password")
 *   Login button      → button#loginButton       (id="loginButton")
 *   Error message     → div.error-message        (NOTE: NOT error-msg — different from user login)
 *   Page heading      → h2  (text "Admin Login")
 *
 * Selectors derived from src/pages/AdminDashboard.jsx:
 *   Sidebar nav items (li elements inside aside.sidebar ul):
 *     "View Users"                 → li with text "View Users"
 *     "Manage Charging Stations"   → li with text "Manage Charging Stations"
 *     "View Statistics"            → li with text "View Statistics"
 *     "View Bookings"              → li with text "View Bookings"
 *
 *   Users tab content       → div#viewUsers
 *   Stations tab content    → div#manageStations   (contains form#addStationForm)
 *   Stats tab content       → div#viewStats        (contains div.stats-overview)
 *   Bookings tab content    → div#viewBookings
 *
 *   Users table             → div#viewUsers table
 *   Add station form        → form#addStationForm
 *   Stats overview          → div.stats-overview
 *   Bookings table          → div#viewBookings table
 *
 *   Admin logout button     → button#logoutBtn
 *
 * Admin credentials: username=admin / password=password123
 *   (hardcoded in AdminLogin.jsx, no Firebase involved)
 *
 * IMPORTANT: AdminLogin calls alert("Login successful!") on valid login.
 * We must dismiss this JavaScript alert before interacting with the dashboard.
 *
 * @BeforeClass navigates to /admin.
 */
public class AdminTest extends BaseTest {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "password123";
    private static final Duration WAIT     = Duration.ofSeconds(15);

    // -----------------------------------------------------------------------
    // @BeforeClass – start at the admin login page
    // -----------------------------------------------------------------------

    @BeforeClass
    public void goToAdminPage() {
        navigateTo("/admin");
    }

    // -----------------------------------------------------------------------
    // Helper: perform admin login and dismiss the success alert
    // -----------------------------------------------------------------------

    private void performAdminLogin() {
        navigateTo("/admin");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement usernameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("username"))
        );
        usernameInput.clear();
        usernameInput.sendKeys(ADMIN_USER);

        driver.findElement(By.id("password")).sendKeys(ADMIN_PASS);
        driver.findElement(By.id("loginButton")).click();

        // AdminLogin.jsx line 27: alert("Login successful!") — must dismiss it
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        // Now wait for the dashboard URL
        wait.until(ExpectedConditions.urlContains("/admin/dashboard"));
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    /**
     * Test 1: Admin login page loads with username and password fields.
     */
    @Test(priority = 1)
    public void testAdminLoginPageLoads() {
        navigateTo("/admin");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // Heading "Admin Login" (h2 in AdminLogin.jsx line 41)
        WebElement heading = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.tagName("h2"))
        );
        Assert.assertEquals(heading.getText().trim(), "Admin Login",
                "Admin login page should have heading 'Admin Login'");

        // Username field (id="username")
        WebElement usernameInput = driver.findElement(By.id("username"));
        Assert.assertTrue(usernameInput.isDisplayed(), "Username input should be visible");

        // Password field (id="password")
        WebElement passwordInput = driver.findElement(By.id("password"));
        Assert.assertTrue(passwordInput.isDisplayed(), "Password input should be visible");
    }

    /**
     * Test 2: Invalid admin credentials show error message.
     * AdminLogin.jsx line 30: setError("Invalid username or password!")
     * Error element: div.error-message  (NOT div.error-msg)
     */
    @Test(priority = 2)
    public void testAdminInvalidLogin() {
        navigateTo("/admin");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement usernameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("username"))
        );
        usernameInput.clear();
        usernameInput.sendKeys("wrongadmin");

        driver.findElement(By.id("password")).sendKeys("wrongpass");
        driver.findElement(By.id("loginButton")).click();

        // Error div: div.error-message  (AdminLogin.jsx line 68)
        WebElement errorDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.error-message"))
        );
        Assert.assertTrue(errorDiv.isDisplayed(), "Error message should appear for wrong admin credentials");
        Assert.assertTrue(errorDiv.getText().toLowerCase().contains("invalid"),
                "Error should say 'Invalid username or password', got: " + errorDiv.getText());
    }

    /**
     * Test 3: Valid admin credentials redirect to the dashboard.
     * Checks for the Admin Portal heading and sidebar navigation.
     */
    @Test(priority = 3)
    public void testAdminValidLogin() {
        performAdminLogin();

        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // AdminDashboard.jsx line 400: <h1 className="admin-title">Admin Portal</h1>
        WebElement adminTitle = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.admin-title"))
        );
        Assert.assertTrue(adminTitle.getText().contains("Admin Portal"),
                "Dashboard should show 'Admin Portal' heading");

        // Sidebar should contain all four tab items
        Assert.assertFalse(driver.findElements(By.xpath(
                "//aside[@class='sidebar']//li[normalize-space(text())='View Users']")).isEmpty(),
                "Sidebar should have 'View Users' tab");

        Assert.assertFalse(driver.findElements(By.xpath(
                "//aside[@class='sidebar']//li[normalize-space(text())='Manage Charging Stations']")).isEmpty(),
                "Sidebar should have 'Manage Charging Stations' tab");

        Assert.assertFalse(driver.findElements(By.xpath(
                "//aside[@class='sidebar']//li[normalize-space(text())='View Statistics']")).isEmpty(),
                "Sidebar should have 'View Statistics' tab");

        Assert.assertFalse(driver.findElements(By.xpath(
                "//aside[@class='sidebar']//li[normalize-space(text())='View Bookings']")).isEmpty(),
                "Sidebar should have 'View Bookings' tab");
    }

    /**
     * Test 4: Clicking "View Users" tab shows the users table.
     * AdminDashboard.jsx line 442: <div id="viewUsers" className="tab-content active">
     */
    @Test(priority = 4, dependsOnMethods = "testAdminValidLogin")
    public void testAdminViewUsersTab() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // Click "View Users" sidebar item
        WebElement viewUsersTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(
                        "//aside[@class='sidebar']//li[normalize-space(text())='View Users']"))
        );
        viewUsersTab.click();

        // div#viewUsers should be present and visible
        WebElement viewUsersDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("viewUsers"))
        );
        Assert.assertTrue(viewUsersDiv.isDisplayed(), "View Users tab content should be visible");

        // A <table> must be present inside #viewUsers (shows headers even if no users)
        WebElement usersTable = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#viewUsers table"))
        );
        Assert.assertNotNull(usersTable, "Users table should be present in the View Users tab");
    }

    /**
     * Test 5: Clicking "Manage Charging Stations" tab shows the add-station form.
     * AdminDashboard.jsx line 485: <div id="manageStations" ...>
     * line 488: <form id="addStationForm">
     */
    @Test(priority = 5, dependsOnMethods = "testAdminValidLogin")
    public void testAdminManageStationsTab() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement stationsTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(
                        "//aside[@class='sidebar']//li[normalize-space(text())='Manage Charging Stations']"))
        );
        stationsTab.click();

        // div#manageStations should appear
        WebElement manageDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("manageStations"))
        );
        Assert.assertTrue(manageDiv.isDisplayed(), "Manage Stations tab content should be visible");

        // Add station form must be present
        WebElement addForm = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("addStationForm"))
        );
        Assert.assertNotNull(addForm, "Add Station form should be present");

        // Station Name input (id="stationName")
        WebElement stationNameInput = driver.findElement(By.id("stationName"));
        Assert.assertTrue(stationNameInput.isDisplayed(), "Station Name input should be visible");
    }

    /**
     * Test 6: "View Statistics" tab shows the statistics overview.
     * AdminDashboard.jsx line 615: <div id="viewStats" ...>
     * line 622: <div className="stats-overview">
     */
    @Test(priority = 6, dependsOnMethods = "testAdminValidLogin")
    public void testAdminViewStatisticsTab() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement statsTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(
                        "//aside[@class='sidebar']//li[normalize-space(text())='View Statistics']"))
        );
        statsTab.click();

        // div#viewStats should appear
        WebElement viewStatsDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("viewStats"))
        );
        Assert.assertTrue(viewStatsDiv.isDisplayed(), "View Statistics tab content should be visible");

        // stats-overview div must be present (shows Total bookings, distance, time)
        // It renders after loadingBookings becomes false
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement statsOverview = longWait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.stats-overview"))
        );
        Assert.assertNotNull(statsOverview,
                "Statistics overview (div.stats-overview) should be present");

        // The overview must mention "Total bookings"
        Assert.assertTrue(statsOverview.getText().contains("Total bookings"),
                "Stats overview should mention 'Total bookings'");
    }

    /**
     * Test 7: "View Bookings" tab shows the all-bookings table.
     * AdminDashboard.jsx line 653: <div id="viewBookings" ...>
     */
    @Test(priority = 7, dependsOnMethods = "testAdminValidLogin")
    public void testAdminViewBookingsTab() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        WebElement bookingsTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(
                        "//aside[@class='sidebar']//li[normalize-space(text())='View Bookings']"))
        );
        bookingsTab.click();

        // div#viewBookings should appear
        WebElement viewBookingsDiv = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("viewBookings"))
        );
        Assert.assertTrue(viewBookingsDiv.isDisplayed(), "View Bookings tab content should be visible");

        // A <table> with booking columns must be present (loaded via onSnapshot)
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement bookingsTable = longWait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#viewBookings table"))
        );
        Assert.assertNotNull(bookingsTable, "Bookings table should be present in the View Bookings tab");
    }
}
