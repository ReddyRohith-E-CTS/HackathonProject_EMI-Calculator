package com.hackathon.stepdefinitions;

import com.hackathon.base.BaseClass;
import com.hackathon.config.ConfigReader;
import com.hackathon.context.ScenarioContext;
import com.hackathon.pages.HomeLoanPage;
import com.hackathon.pages.HomePage;
import com.hackathon.utils.ExcelUtils;
import com.hackathon.utils.TestDataReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.File;
import java.util.Map;

// Step definitions for the Home Loan Yearly Schedule feature (TC05–TC06).
// Covers menu navigation, amortisation table extraction, and Excel file output.
public class HomeLoanSteps {

    private final ScenarioContext ctx;

    // PicoContainer injects the shared ScenarioContext.
    public HomeLoanSteps(ScenarioContext ctx) { this.ctx = ctx; }

    // Loads the row from TestData.xlsx sheet 'HomeLoan' for this TC and caches it.
    @Given("I have home loan test data for {string}")
    public void load_home_data(String tcId) {
        Map<String, String> data = TestDataReader.get("HomeLoan", tcId);
        ctx.put("testData", data);
        ctx.put("tcId", tcId);
    }

    // Reads MenuItem from test data, hovers the top menu and clicks it.
    @When("I open the menu item from the test data")
    public void open_menu_from_data() {
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) ctx.get("testData");
        if (ctx.homePage == null) ctx.homePage = new HomePage().open();
        String item = data.get("MenuItem");
        if ("Home Loan EMI Calculator".equalsIgnoreCase(item)) {
            ctx.homeLoanPage = new HomeLoanPage().openViaMenu(ctx.homePage);
        } else {
            throw new IllegalArgumentException("Unsupported menu item: " + item);
        }
    }

    // Soft-asserts the current URL contains the home-loan-emi-calculator path.
    @Then("the Home Loan EMI Calculator page should load")
    public void verify_home_loan_loaded() {
        ctx.softly.assertThat(BaseClass.getDriver().getCurrentUrl())
                .as("Home Loan URL").contains("home-loan-emi-calculator");
    }

    // Opens the Home Loan calculator page directly via its URL.
    @Given("the Home Loan EMI Calculator page is open")
    public void open_home_loan_page() { ctx.homeLoanPage = new HomeLoanPage().openDirect(); }

    // Fills the form using LoanAmount/InterestRate/Tenure from the test data row.
    @When("I fill the home loan form with values from the test data")
    public void fill_from_data() {
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) ctx.get("testData");
        ctx.homeLoanPage.fillForm(data.get("LoanAmount"), data.get("InterestRate"), data.get("Tenure"));
    }

    // Extracts the year-on-year schedule grid into the scenario context.
    @And("I extract the year-on-year schedule")
    public void extract_schedule() { ctx.extractedSchedule = ctx.homeLoanPage.extractYearlySchedule(); }

    // Soft-asserts the schedule has at least the MinRows defined in the test data.
    @Then("the schedule should have the minimum yearly rows defined in the test data")
    public void schedule_min_rows() {
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) ctx.get("testData");
        int min = Integer.parseInt(data.get("MinRows"));
        ctx.softly.assertThat(ctx.extractedSchedule.size() - 1)
                .as("Yearly rows extracted").isGreaterThanOrEqualTo(min);
    }

    // Writes the extracted schedule to output/HomeLoan_YearlySchedule.xlsx via POI.
    @And("the schedule is stored in the Home Loan Excel file")
    public void store_to_excel() {
        // Use a browser-specific filename so Chrome and Edge never write to the same file.
        String base = ConfigReader.get().get("excel.home.loan.file");
        String path = base.replace(".xlsx", "_" + BaseClass.getBrowserName() + ".xlsx");
        ExcelUtils.writeSheet(path, "YearlySchedule", ctx.extractedSchedule);
        ctx.put("homeLoanExcelPath", path);
    }

    // Soft-asserts the Home Loan Excel file exists on disk and is non-empty.
    @And("the Home Loan Excel file should exist on disk")
    public void verify_excel_on_disk() {
        File f = new File((String) ctx.get("homeLoanExcelPath"));
        ctx.softly.assertThat(f.exists() && f.length() > 0)
                .as("Excel not created: %s", f.getAbsolutePath()).isTrue();
    }
}
