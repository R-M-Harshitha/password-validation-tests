package com.bitm.tests;

import java.io.File;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Course Project No. 24 - Software Testing (22CS72)
 * Password Strength and Confirm Password Validation Tests Using Selenium.
 */
public class PasswordValidationTest {

    private WebDriver driver;

    // Locators
    private final By username        = By.id("username");
    private final By password        = By.id("password");
    private final By confirmPassword = By.id("confirmPassword");
    private final By registerBtn     = By.id("registerBtn");
    private final By strengthLabel   = By.id("strengthLabel");
    private final By usernameError   = By.id("usernameError");
    private final By passwordError   = By.id("passwordError");
    private final By confirmError    = By.id("confirmError");
    private final By successMsg      = By.id("successMsg");

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Run with  mvn test -Dheadless=true  on Jenkins (no visible browser window)
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new", "--window-size=1366,768");
        }
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.get(new File("src/test/resources/register.html").toURI().toString());
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- helper methods ----------
    private void type(By locator, String text) {
        driver.findElement(locator).clear();
        if (text != null && !text.isEmpty()) {
            driver.findElement(locator).sendKeys(text);
        }
    }

    private String text(By locator) {
        return driver.findElement(locator).getText().trim();
    }

    private void fillAndRegister(String user, String pass, String confirm) {
        type(username, user);
        type(password, pass);
        type(confirmPassword, confirm);
        driver.findElement(registerBtn).click();
    }

    // ---------- test data ----------
    @DataProvider(name = "strengthData")
    public Object[][] strengthData() {
        return new Object[][] {
            // password,     expected strength
            { "abc",         "Weak"   },   // too short
            { "abcdefgh",    "Weak"   },   // 8 chars, one type only
            { "12345678",    "Weak"   },   // digits only
            { "Abc@123",     "Weak"   },   // boundary: 7 chars, all types
            { "abcd1234",    "Medium" },   // letters + digits
            { "Abcdefg1",    "Medium" },   // 3 character types
            { "Abc@1234",    "Strong" },   // boundary: 8 chars, all types
            { "Hello@123",   "Strong" }    // 9 chars, all types
        };
    }

    // ---------- test cases ----------

    @Test(priority = 1, description = "TC01 - Empty password shows required error")
    public void tc01_emptyPasswordShowsError() {
        fillAndRegister("student01", "", "");
        Assert.assertEquals(text(passwordError), "Password is required");
        Assert.assertEquals(text(successMsg), "");
    }

    @Test(priority = 2, dataProvider = "strengthData",
          description = "TC02-TC09 - Strength indicator for different passwords")
    public void tc02_passwordStrengthIndicator(String pass, String expected) {
        type(password, pass);
        Assert.assertEquals(text(strengthLabel), expected,
                "Wrong strength shown for password: " + pass);
    }

    @Test(priority = 3, description = "TC10 - Strong password with matching confirm registers successfully")
    public void tc10_matchingPasswordsSucceed() {
        fillAndRegister("student01", "Hello@123", "Hello@123");
        Assert.assertEquals(text(passwordError), "");
        Assert.assertEquals(text(confirmError), "");
        Assert.assertEquals(text(successMsg), "Registration successful");
    }

    @Test(priority = 4, description = "TC11 - Mismatching passwords show error")
    public void tc11_mismatchShowsError() {
        fillAndRegister("student01", "Hello@123", "Hello@124");
        Assert.assertEquals(text(confirmError), "Passwords do not match");
        Assert.assertEquals(text(successMsg), "");
    }

    @Test(priority = 5, description = "TC12 - Empty confirm password shows required error")
    public void tc12_emptyConfirmShowsError() {
        fillAndRegister("student01", "Hello@123", "");
        Assert.assertEquals(text(confirmError), "Confirm password is required");
        Assert.assertEquals(text(successMsg), "");
    }

    @Test(priority = 6, description = "TC13 - Confirm password is case sensitive")
    public void tc13_confirmIsCaseSensitive() {
        fillAndRegister("student01", "Hello@123", "hello@123");
        Assert.assertEquals(text(confirmError), "Passwords do not match");
    }

    @Test(priority = 7, description = "TC14 - Password with spaces is rejected")
    public void tc14_spacesNotAllowed() {
        fillAndRegister("student01", "Hello @123", "Hello @123");
        Assert.assertEquals(text(passwordError), "Password must not contain spaces");
        Assert.assertEquals(text(successMsg), "");
    }

    @Test(priority = 8, description = "TC15 - Weak password is blocked even when both fields match")
    public void tc15_weakPasswordBlocked() {
        fillAndRegister("student01", "abc123", "abc123");
        Assert.assertEquals(text(passwordError), "Password is too weak");
        Assert.assertEquals(text(successMsg), "");
    }

    @Test(priority = 9, description = "TC16 - Mismatch error appears live while typing confirm password")
    public void tc16_liveMismatchWhileTyping() {
        type(password, "Hello@123");
        type(confirmPassword, "Hello");
        Assert.assertEquals(text(confirmError), "Passwords do not match");
        type(confirmPassword, "Hello@123");
        Assert.assertEquals(text(confirmError), "", "Error should clear once passwords match");
    }

    @Test(priority = 10, description = "TC17 - Empty username is rejected")
    public void tc17_emptyUsernameShowsError() {
        fillAndRegister("", "Hello@123", "Hello@123");
        Assert.assertEquals(text(usernameError), "Username is required");
        Assert.assertEquals(text(successMsg), "");
    }
}
