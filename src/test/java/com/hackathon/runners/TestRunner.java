package com.hackathon.runners;

import com.hackathon.base.BaseClass;
import io.cucumber.testng.CucumberOptions;

// Single test runner used for all browsers. 
// The html: and json: report plugins are NOT declared here - BaseClass.setUpClass() injects them via the cucumber.plugin system property so Chrome and Edge each write to their own report file without a race condition.
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.hackathon.stepdefinitions",
                "com.hackathon.hooks"
        },
        tags = "@Smoke or @Regression or @UI",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends BaseClass {
    // All driver lifecycle and parallel execution logic lives in BaseClass.
}
