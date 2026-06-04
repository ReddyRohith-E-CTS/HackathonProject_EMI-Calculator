package com.hackathon.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.hackathon.base.BaseClass;

// Utility for capturing and saving full-window screenshots via WebDriver's TakesScreenshot.
// Files are saved under screenshots/ with a timestamped, filename-safe name.
public final class ScreenshotUtils {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private ScreenshotUtils() {
    }

    // Captures a PNG of the current window, saves it under <project>/screenshots/,
    // returns the absolute path.
    public static String capture(String name) {
        try {
            File source = ((TakesScreenshot) BaseClass.getDriver()).getScreenshotAs(OutputType.FILE);
            String safe = name.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path target = Path.of(System.getProperty("user.dir"), "screenshots",
                    safe + "_" + LocalDateTime.now().format(TS) + ".png");
            Files.createDirectories(target.getParent());
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved -> " + target.toAbsolutePath());
            return target.toAbsolutePath().toString();
        } catch (Exception e) {
            System.out.println("Screenshot error -> " + e.getMessage());
            return null;
        }
    }
}
