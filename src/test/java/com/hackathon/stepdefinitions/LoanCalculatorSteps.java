package com.hackathon.stepdefinitions;

import com.hackathon.context.ScenarioContext;
import com.hackathon.pages.LoanCalculatorPage;
import com.hackathon.utils.TestDataReader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.Map;

// Step definitions for the Loan Calculator UI feature (TC07–TC10).
// Covers sub-tab selection, input validation, slider visibility, and tenure unit toggling.
public class LoanCalculatorSteps {

    private final ScenarioContext ctx;

    // PicoContainer injects the shared ScenarioContext.
    public LoanCalculatorSteps(ScenarioContext ctx) {
        this.ctx = ctx;
    }

    // Opens the Loan Calculator page directly via its URL.
    @Given("the Loan Calculator page is open")
    public void open_loan_calc() {
        ctx.loanCalculatorPage = new LoanCalculatorPage().openDirect();
    }

    // Loads the row from TestData.xlsx sheet 'LoanCalculator' for this TC and caches it.
    @Given("I have loan calculator test data for {string}")
    public void load_loan_calc_data(String tcId) {
        Map<String, String> data = TestDataReader.get("LoanCalculator", tcId);
        ctx.put("testData", data);
        ctx.put("tcId", tcId);
    }

    // Reads SubTab from the test data and clicks that sub-tab.
    @When("I select the sub-tab from the test data")
    public void select_sub_tab_from_data() {
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) ctx.get("testData");
        switch (data.get("SubTab")) {
            case "EMI Calculator" -> ctx.loanCalculatorPage.selectEmiCalculator();
            case "Loan Amount Calculator" -> ctx.loanCalculatorPage.selectAmountCalculator();
            case "Loan Tenure Calculator" -> ctx.loanCalculatorPage.selectTenureCalculator();
            default -> throw new IllegalArgumentException("Unknown sub-tab: " + data.get("SubTab"));
        }
    }

    // Soft-asserts loan amount, interest and tenure text boxes are enabled.
    @Then("all visible input fields should be enabled")
    public void verify_inputs_enabled() {
        LoanCalculatorPage.UIValidationResult r = ctx.loanCalculatorPage.validateInputs();
        ctx.softly.assertThat(r.loanAmountEnabled).as("Loan amount enabled").isTrue();
        ctx.softly.assertThat(r.interestEnabled).as("Interest enabled").isTrue();
        ctx.softly.assertThat(r.tenureEnabled).as("Tenure enabled").isTrue();
    }

    // Soft-asserts at least three slider handles are visible on the current sub-tab.
    @And("all sliders should be displayed")
    public void verify_sliders_displayed() {
        ctx.softly.assertThat(ctx.loanCalculatorPage.areAllSlidersDisplayed())
                .as("All sliders rendered").isTrue();
    }

    // Captures the tenure scale tick markers before any toggle changes them.
    @And("I capture the tenure scale signature")
    public void capture_signature() {
        ctx.tenureScaleSignatureBefore = ctx.loanCalculatorPage.tenureScaleSignature();
    }

    // Flips tenure unit to Yr or Mo and re-reads the new scale signature.
    @And("I switch the tenure unit to {string}")
    public void switch_unit(String unit) {
        if (unit.toLowerCase().startsWith("mo"))
            ctx.loanCalculatorPage.switchTenureToMonths();
        else
            ctx.loanCalculatorPage.switchTenureToYears();
        ctx.tenureScaleSignatureAfter = ctx.loanCalculatorPage.tenureScaleSignature();
    }

    // Soft-asserts the new scale signature differs from the captured baseline.
    @Then("the tenure scale signature should change")
    public void signature_should_change() {
        ctx.softly.assertThat(ctx.tenureScaleSignatureAfter)
                .as("Scale changed").isNotEqualTo(ctx.tenureScaleSignatureBefore);
    }

    // Soft-asserts the scale signature returns to the original after flipping back.
    @Then("the tenure scale signature should match the original")
    public void signature_should_match_original() {
        ctx.softly.assertThat(ctx.loanCalculatorPage.tenureScaleSignature())
                .as("Scale returns to original").isEqualTo(ctx.tenureScaleSignatureBefore);
    }
}
