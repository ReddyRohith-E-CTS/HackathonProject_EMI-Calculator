package com.hackathon.pages;

import com.hackathon.base.BaseClass;
import com.hackathon.config.ConfigReader;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

// Abstract base for all Page Object classes; provides the shared WebDriver, explicit wait, and reusable helpers (click, type, scroll, JS) used by every page class.
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    // Captures the current driver, sets up the explicit wait, and initialises
    // @FindBy fields.
    protected BasePage() {
        this.driver = BaseClass.getDriver();
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.get().getInt("explicit.wait.seconds")));
        PageFactory.initElements(driver, this);
    }

    // Navigates to a URL and waits for document.readyState=complete.
    protected void open(String url) {
        driver.get(url);
        waitForReady();
    }

    // Blocks until the page reports complete via document.readyState.
    protected void waitForReady() {
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    // Waits until the element is visible and returns it.
    protected WebElement waitVisible(WebElement el) {
        return wait.until(ExpectedConditions.visibilityOf(el));
    }

    // Waits until the element is clickable and returns it.
    protected WebElement waitClickable(WebElement el) {
        return wait.until(ExpectedConditions.elementToBeClickable(el));
    }

    // Clicks an element with a JS fallback if the native click is intercepted.
    protected void click(WebElement el) {
        WebElement e = waitClickable(el);
        scrollIntoView(e);
        try {
            e.click();
        } catch (Exception ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", e);
        }
    }

    // Clears an input by selecting-all and types the new value, then Tabs out.
    protected void typeReplacing(WebElement el, String value) {
        WebElement e = waitVisible(el);
        scrollIntoView(e);
        e.click();
        e.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        e.sendKeys(Keys.DELETE);
        e.sendKeys(value);
        e.sendKeys(Keys.TAB);
    }

    // Returns the trimmed visible text of an element.
    protected String text(WebElement el) {
        return waitVisible(el).getText().trim();
    }

    // Safe isDisplayed() that returns false instead of throwing.
    protected boolean isVisible(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    // Safe isEnabled() that returns false instead of throwing.
    protected boolean isEnabledSafe(WebElement el) {
        try {
            return el.isEnabled();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    // Scrolls the element into the centre of the viewport via JS.
    protected void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    // Scrolls the window by the given delta.
    protected void scrollBy(int x, int y) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    // Runs an arbitrary JavaScript snippet against the current page.
    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    // Waits until the given JS expression evaluates to a truthy value.
    protected void waitForJsTruthy(String expression) {
        wait.until(d -> Boolean.TRUE.equals(
                ((JavascriptExecutor) d).executeScript("return Boolean(" + expression + ");")));
    }

    // Returns an Actions builder bound to the current driver.
    protected Actions actions() {
        return new Actions(driver);
    }

    // Picks the first element from a list whose visible text equals value.
    protected WebElement findInListByText(List<WebElement> elements, String value) {
        return elements.stream()
                .filter(e -> {
                    try {
                        return e.getText().trim().equals(value);
                    } catch (StaleElementReferenceException ex) {
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No element with text '" + value + "'"));
    }
}
