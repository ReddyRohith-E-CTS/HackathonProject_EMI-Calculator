package com.hackathon.hooks;

import com.aventstack.extentreports.Status;
import com.hackathon.base.BaseClass;
import com.hackathon.context.ScenarioContext;
import com.hackathon.reports.ExtentManager;
import com.hackathon.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;

// Cucumber hooks that wrap every scenario: open an Extent node before, capture a
// screenshot and flush soft assertions after, regardless of pass or fail.
public class Hooks {

    private final ScenarioContext ctx;

    // PicoContainer injects a fresh ScenarioContext per scenario.
    public Hooks(ScenarioContext ctx) { this.ctx = ctx; }

    // Opens an ExtentTest, logs the start banner and clears session cookies.
    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        String browser = currentBrowser();
        String tcId = extractTcId(scenario);
        ExtentManager.startScenario(
                String.format("[%s] %s %s", browser, tcId, scenario.getName()),
                "Source: " + scenario.getUri());
        ExtentManager.logStep(Status.INFO, "Starting on <b>" + browser + "</b>");
        try { BaseClass.getDriver().manage().deleteAllCookies(); }
        catch (Exception ignored) {}
    }

    // Captures a screenshot, attaches it to Extent, then flushes soft asserts.
    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        String browser = currentBrowser();
        String tcId    = extractTcId(scenario);
        boolean softFailed = !ctx.softly.errorsCollected().isEmpty();
        boolean failed     = scenario.isFailed() || softFailed;
        String  status     = failed ? "FAILED" : "PASSED";

        String safeName = stripTcPrefix(scenario.getName());
        String name = String.format("%s_%s_%s_%s",
                tcId, sanitise(safeName), status, browser);
        String path = ScreenshotUtils.capture(name);

        if (path != null) {
            String label = String.format("%s - %s on %s", tcId, status, browser);
            if (failed) ExtentManager.attachFailureScreenshot(path, label);
            else        ExtentManager.attachPassScreenshot(path, label);
        }
        if (!failed) ExtentManager.logStep(Status.PASS, "Scenario passed");

        try {
            ctx.softly.assertAll();
        } finally {
            ExtentManager.endScenario();
        }
    }

    // Extracts the @TCxx tag if present, else parses TCxx from the scenario name.
    private String extractTcId(Scenario scenario) {
        String fromTag = scenario.getSourceTagNames().stream()
                .filter(t -> t.matches("@TC\\d+"))
                .findFirst()
                .map(t -> t.substring(1))
                .orElse(null);
        if (fromTag != null) return fromTag;
        String name = scenario.getName();
        if (name != null) {
            var m = java.util.regex.Pattern.compile("^(TC\\d+)").matcher(name);
            if (m.find()) return m.group(1);
        }
        return "TCXX";
    }

    // Reduces a scenario name to a filename-safe slug capped at 60 chars.
    private String sanitise(String s) {
        if (s == null) return "scenario";
        String slug = s.replaceAll("[^a-zA-Z0-9]+", "_");
        return slug.length() > 60 ? slug.substring(0, 60) : slug;
    }

    // Drops a leading "TCxx - " or "TCxx_" so the filename does not duplicate the tag.
    private String stripTcPrefix(String name) {
        if (name == null) return "";
        return name.replaceFirst("^TC\\d+\\s*[-_:]?\\s*", "");
    }

    // Reads the browser name from the active WebDriver's capabilities.
    private String currentBrowser() {
        try {
            WebDriver d = BaseClass.getDriver();
            if (d instanceof HasCapabilities hc) {
                String name = hc.getCapabilities().getBrowserName();
                return (name == null || name.isBlank()) ? "unknown" : name.toLowerCase();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }
}
