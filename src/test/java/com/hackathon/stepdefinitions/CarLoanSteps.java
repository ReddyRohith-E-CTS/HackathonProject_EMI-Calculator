package com.hackathon.stepdefinitions;

import com.hackathon.base.BaseClass;
import com.hackathon.config.ConfigReader;
import com.hackathon.context.ScenarioContext;
import com.hackathon.pages.HomePage;
import com.hackathon.utils.EMICalculatorUtil;
import com.hackathon.utils.ExcelUtils;
import com.hackathon.utils.TestDataReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.File;
import java.util.List;
import java.util.Map;

// Step definitions for the Car Loan EMI feature (TC01–TC04).
// Covers loan input, EMI/interest/principal verification, and Excel export.
public class CarLoanSteps {

    private final ScenarioContext ctx;

    // PicoContainer injects the shared ScenarioContext.
    public CarLoanSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // Opens the EMI Calculator homepage.
    @Given("the EMI Calculator homepage is open")
    public void open_homepage() {
        ctx.homePage = new HomePage().open();
    }

    // Loads the row from TestData.xlsx sheet 'CarLoan' for this TC and caches it.
    @Given("I have car loan test data for {string}")
    public void load_car_data(String tcId) {
        Map<String, String> data = TestDataReader.get("CarLoan", tcId);
        ctx.put("testData", data);
        ctx.put("tcId", tcId);
        ctx.put("principal", Double.parseDouble(data.get("LoanAmount")));
        ctx.put("rate", Double.parseDouble(data.get("InterestRate")));
        int tenure = Integer.parseInt(data.get("Tenure"));
        boolean months = data.get("TenureUnit").toLowerCase().startsWith("mo");
        ctx.put("months", months ? tenure : tenure * 12);
    }

    // Clicks the Car Loan tab and types the inputs read from the test data row.
    @When("I select the Car Loan tab and enter the loan details from the test data")
    public void enter_loan_details_from_data() {
        Map<String, String> data = ctx.get("testData");
        HomePage.TenureUnit u = data.get("TenureUnit").toLowerCase().startsWith("mo")
                ? HomePage.TenureUnit.MONTHS
                : HomePage.TenureUnit.YEARS;
        ctx.homePage.selectCarLoanTab()
                .enterLoanAmount(data.get("LoanAmount"))
                .enterInterestRate(data.get("InterestRate"))
                .enterTenure(data.get("Tenure"), u);
    }

    // Reads VerificationType + Year from the test data and dispatches the right assertion.
    @Then("the configured calculation should match the formula")
    public void verify_configured() {
        Map<String, String> data = ctx.get("testData");
        String check = data.get("VerificationType");
        int year = Integer.parseInt(data.get("Year"));
        switch (check) {
            case "emi" -> verify_emi();
            case "first_month_interest" -> verify_first_month_interest(year);
            case "first_month_principal" -> verify_first_month_principal(year);
            case "excel_export" -> {
                /* handled by separate steps */ }
            default -> throw new IllegalArgumentException("Unknown VerificationType: " + check);
        }
    }

    // Soft-asserts the displayed EMI matches the formula within tolerance.
    private void verify_emi() {
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        int m = ctx.get("months");
        long expected = Math.round(EMICalculatorUtil.emi(p, r, m));
        long actual = ctx.homePage.readEmi();
        long tol = ConfigReader.get().getInt("emi.tolerance");
        ctx.softly.assertThat(Math.abs(actual - expected))
                .as("EMI: expected %d +/- %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    // Soft-asserts the first month interest matches Principal x monthly rate.
    private void verify_first_month_interest(int year) {
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        long expected = Math.round(EMICalculatorUtil.firstMonthInterest(p, r));
        long actual = ctx.homePage.readFirstMonthInterest(year);
        long tol = ConfigReader.get().getInt("emi.tolerance");
        ctx.softly.assertThat(Math.abs(actual - expected))
                .as("First month interest: expected %d +/- %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    // Soft-asserts the first month principal equals EMI minus first month interest.
    private void verify_first_month_principal(int year) {
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        int m = ctx.get("months");
        long expected = Math.round(EMICalculatorUtil.firstMonthPrincipal(p, r, m));
        long actual = ctx.homePage.readFirstMonthPrincipal(year);
        long tol = ConfigReader.get().getInt("emi.tolerance");
        ctx.softly.assertThat(Math.abs(actual - expected))
                .as("First month principal: expected %d +/- %d, got %d", expected, tol, actual)
                .isLessThanOrEqualTo(tol);
    }

    // Writes the EMI summary (8 rows) to output/CarLoan_EMI_Summary.xlsx via POI.
    @Then("the car loan EMI summary is written to Excel")
    public void write_car_summary() {
        Map<String, String> data = ctx.get("testData");
        double p = ctx.get("principal");
        double r = ctx.get("rate");
        int m = ctx.get("months");
        int year = Integer.parseInt(data.get("Year"));
        List<List<String>> rows = List.of(
                List.of("Field", "Value"),
                List.of("Principal (Rs.)", String.valueOf((long) p)),
                List.of("Interest Rate (% p.a.)", String.valueOf(r)),
                List.of("Tenure (months)", String.valueOf(m)),
                List.of("EMI (Rs.)", String.valueOf(ctx.homePage.readEmi())),
                List.of("First Month Interest (Rs.)", String.valueOf(ctx.homePage.readFirstMonthInterest(year))),
                List.of("First Month Principal (Rs.)", String.valueOf(ctx.homePage.readFirstMonthPrincipal(year))),
                List.of("Total Interest (Rs.)", String.valueOf(ctx.homePage.readTotalInterest())));
        // Use a browser-specific filename so Chrome and Edge never write to the same
        // file.
        String base = ConfigReader.get().get("excel.car.loan.file");
        String path = base.replace(".xlsx", "_" + BaseClass.getBrowserName() + ".xlsx");
        ExcelUtils.writeSheet(path, "CarLoanEMI", rows);
        ctx.put("carExcelPath", path);
    }

    // Soft-asserts the generated Excel file exists and has at least N rows.
    @And("the Excel file should have at least {int} rows")
    public void excel_should_have_rows(int rows) {
        String path = ctx.get("carExcelPath");
        File f = new File(path);
        ctx.softly.assertThat(f.exists()).as("Excel not created: %s", path).isTrue();
        if (f.exists()) {
            ctx.softly.assertThat(ExcelUtils.rowCount(path, "CarLoanEMI"))
                    .as("Row count in %s", path).isGreaterThanOrEqualTo(rows);
        }
    }
}
