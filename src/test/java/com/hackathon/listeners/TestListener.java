package com.hackathon.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.hackathon.reportmanager.ExtentManager;

import java.io.File;

// TestNG listener that cleans stale artefacts before each suite run, flushes the
// Extent report after the suite, and logs every test lifecycle event via Log4j 2.
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);
    private static final String SCREENSHOTS_DIR    = "screenshots";
    private static final String ALLURE_RESULTS_DIR = "target/allure-results";
    private static final String CUCUMBER_REPORTS_DIR = "reports/cucumber";
    private static final String TESTNG_REPORTS_DIR   = "reports/testng";

    // Wipes per-run artefacts so each suite starts clean.
    // reports/extent/ is intentionally NOT cleaned - every Extent report has
    // a unique timestamp, so runs accumulate side-by-side without overwriting.
    @Override
    public void onStart(ISuite suite) {
        cleanFolder(SCREENSHOTS_DIR,      name -> name.toLowerCase().endsWith(".png"));
        cleanFolder(ALLURE_RESULTS_DIR,   name -> true);
        cleanFolder(CUCUMBER_REPORTS_DIR, name -> (name.endsWith(".html") || name.endsWith(".json")) && name.contains("cucumber"));
        cleanFolder(TESTNG_REPORTS_DIR,   name -> true);
        log.info("SUITE START: {}", suite.getName());
    }

    // Flushes the Extent report once both browser threads finish.
    @Override
    public void onFinish(ISuite suite) {
        ExtentManager.flush();
        log.info("SUITE END: {}", suite.getName());
    }

    // Deletes every file in dirPath matching the predicate. Creates dir if missing.
    private void cleanFolder(String dirPath, java.util.function.Predicate<String> match) {
        File dir = new File(dirPath);
        if (!dir.exists()) { dir.mkdirs(); return; }
        File[] files = dir.listFiles((d, name) -> match.test(name));
        if (files == null) return;
        int deleted = 0;
        for (File f : files) if (f.isFile() && f.delete()) deleted++;
        if (deleted > 0) log.info("Cleared {} stale file(s) from {}", deleted, dirPath);
    }

    // Logs the start of an individual TestNG test method.
    @Override public void onTestStart(ITestResult r)   { log.info("TEST START: {}", r.getMethod().getMethodName()); }

    // Logs a passing TestNG test method.
    @Override public void onTestSuccess(ITestResult r) { log.info("TEST PASS:  {}", r.getMethod().getMethodName()); }

    // Logs a skipped TestNG test method.
    @Override public void onTestSkipped(ITestResult r) { log.warn("TEST SKIP:  {}", r.getMethod().getMethodName()); }

    // Logs a failing TestNG test method with the underlying exception.
    @Override
    public void onTestFailure(ITestResult r) {
        log.error("TEST FAIL:  {} - {}", r.getMethod().getMethodName(), String.valueOf(r.getThrowable()));
    }

    // Logs the start of a TestNG <test> context.
    @Override public void onStart(ITestContext c)  { log.info("Context START: {}", c.getName()); }

    // Logs context end with pass/fail/skip counters.
    @Override
    public void onFinish(ITestContext c) {
        log.info("Context END: {} (passed={}, failed={}, skipped={})",
                c.getName(), c.getPassedTests().size(), c.getFailedTests().size(), c.getSkippedTests().size());
    }
}
