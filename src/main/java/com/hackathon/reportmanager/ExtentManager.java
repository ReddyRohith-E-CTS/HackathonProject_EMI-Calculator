package com.hackathon.reportmanager;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

// Driven directly (not via ExtentCucumberAdapter — that adapter has a Gson concurrency bug under parallel runs). Configuration is read from
// src/test/resources/extent.properties. ThreadLocal isolates the current
// ExtentTest per runner thread.
public final class ExtentManager {

    private static final ExtentReports extent = new ExtentReports();
    private static final ThreadLocal<ExtentTest> TL_TEST = new ThreadLocal<>();
    private static final String REPORT_PATH;

    static {
        Properties p = loadProps("extent.properties");
        String basePath = p.getProperty("extent.report.path", "reports/extent/ExtentReport");
        boolean stamped = Boolean.parseBoolean(p.getProperty("extent.report.timestamp", "true"));
        String stampFmt = p.getProperty("extent.report.timestamp.format", "yyyyMMdd_HHmmss");
        String theme = p.getProperty("extent.report.theme", "DARK");
        String docTitle = p.getProperty("extent.report.document.title", "EMI Calculator Automation Report");
        String repName = p.getProperty("extent.report.report.name", "EMI Calculator Hackathon Project");

        REPORT_PATH = stamped
                ? basePath + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(stampFmt)) + ".html"
                : basePath + ".html";

        new File(REPORT_PATH).getAbsoluteFile().getParentFile().mkdirs();
        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setTheme("DARK".equalsIgnoreCase(theme) ? Theme.DARK : Theme.STANDARD);
        spark.config().setDocumentTitle(docTitle);
        spark.config().setReportName(repName);
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        extent.attachReporter(spark);
        extent.setSystemInfo("Project", "EMI Calculator Hackathon Project");
        extent.setSystemInfo("Site", "emicalculator.net");
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
    }

    private ExtentManager() {
    }

    // Creates a new ExtentTest for the current scenario and binds it to this thread.
    public static synchronized ExtentTest startScenario(String name, String description) {
        ExtentTest test = extent.createTest(name, description);
        TL_TEST.set(test);
        return test;
    }

    // Appends a status line (INFO/PASS/FAIL/etc.) to the current scenario's report node.
    public static void logStep(Status status, String details) {
        ExtentTest t = TL_TEST.get();
        if (t != null)
            t.log(status, details);
    }

    // Attaches a screenshot from a file path as failure evidence.
    public static void attachFailureScreenshot(String filePath, String label) {
        attach(filePath, label, true);
    }

    // Attaches a screenshot from a file path as pass evidence.
    public static void attachPassScreenshot(String filePath, String label) {
        attach(filePath, label, false);
    }

    // Shared screenshot attach helper: logs as fail() or pass() depending on flag.
    private static void attach(String filePath, String label, boolean fail) {
        ExtentTest t = TL_TEST.get();
        if (t == null || filePath == null)
            return;
        try {
            var media = MediaEntityBuilder.createScreenCaptureFromPath(filePath).build();
            if (fail)
                t.fail(label, media);
            else
                t.pass(label, media);
        } catch (Exception ignored) {
        }
    }

    // Clears the per-thread ExtentTest binding (call from Hooks.@After).
    public static void endScenario() {
        TL_TEST.remove();
    }

    // Writes the accumulated report to disk; call once after the suite ends.
    public static synchronized void flush() {
        extent.flush();
    }

    // Returns the absolute path of the HTML report being written this run.
    public static String reportPath() {
        return REPORT_PATH;
    }

    // Loads a properties file from the classpath; returns empty Properties on miss.
    private static Properties loadProps(String fileName) {
        Properties p = new Properties();
        try (InputStream is = ExtentManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is != null)
                p.load(is);
        } catch (Exception ignored) {
        }
        return p;
    }
}
