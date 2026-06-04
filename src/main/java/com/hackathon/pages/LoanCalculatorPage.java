package com.hackathon.pages;

import com.hackathon.config.ConfigReader;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

// Page Object for the Loan Calculator page (EMI / Loan Amount / Loan Tenure sub-tabs).
// Provides UI validation helpers and the tenure-scale signature used in TC08.
public class LoanCalculatorPage extends BasePage {

    @FindBy(id = "menu-item-dropdown-2696")
    private WebElement loanCalcMenu;
    @FindBy(linkText = "Loan Calculator")
    private WebElement loanCalculatorLink;

    @FindBy(xpath = "//a[normalize-space()='EMI Calculator']")
    private WebElement tabEmi;
    @FindBy(xpath = "//a[normalize-space()='Loan Amount Calculator']")
    private WebElement tabAmount;
    @FindBy(xpath = "//a[normalize-space()='Loan Tenure Calculator']")
    private WebElement tabTenure;

    @FindBy(name = "loanamount")
    private WebElement loanAmount;
    @FindBy(name = "loaninterest")
    private WebElement loanInterest;
    @FindBy(name = "loanterm")
    private WebElement loanTerm;
    @FindBy(name = "loanemi")
    private List<WebElement> loanEmiOptional;

    @FindBy(xpath = "//label[input[@id='loanyears']]")
    private WebElement tenureYearsBtn;
    @FindBy(xpath = "//label[input[@id='loanmonths']]")
    private WebElement tenureMonthsBtn;

    // Generic slider locator: different sub-tabs use different specific IDs.
    @FindBy(css = "div.ui-slider .ui-slider-handle")
    private List<WebElement> allSliderHandles;

    @FindBy(css = "#loantermsteps .tick .marker")
    private List<WebElement> tenureScaleTicks;

    // Opens the Loan Calculator page via the top menu.
    public LoanCalculatorPage openViaMenu() {
        actions().moveToElement(loanCalcMenu).perform();
        click(loanCalcMenu);
        wait.until(ExpectedConditions.visibilityOf(loanCalculatorLink));
        click(loanCalculatorLink);
        waitForReady();
        return this;
    }

    // Opens the Loan Calculator page directly via its URL.
    public LoanCalculatorPage openDirect() {
        super.open(ConfigReader.get().get("loan.calculator.url"));
        return this;
    }

    // Switches to the EMI Calculator sub-tab.
    public LoanCalculatorPage selectEmiCalculator() {
        click(tabEmi);
        return this;
    }

    // Switches to the Loan Amount Calculator sub-tab.
    public LoanCalculatorPage selectAmountCalculator() {
        click(tabAmount);
        return this;
    }

    // Switches to the Loan Tenure Calculator sub-tab.
    public LoanCalculatorPage selectTenureCalculator() {
        click(tabTenure);
        return this;
    }

    // Returns true if the loan amount text box accepts input.
    public boolean isLoanAmountTextBoxEnabled() {
        return isEnabledSafe(loanAmount);
    }

    // Returns true if the interest rate text box accepts input.
    public boolean isInterestTextBoxEnabled() {
        return isEnabledSafe(loanInterest);
    }

    // Returns true if the loan tenure text box accepts input.
    public boolean isTenureTextBoxEnabled() {
        return isEnabledSafe(loanTerm);
    }

    // Returns true if the EMI text box is rendered AND enabled (only on Amount/Tenure tabs).
    public boolean isEmiTextBoxEnabled() {
        return !loanEmiOptional.isEmpty() && isEnabledSafe(loanEmiOptional.get(0));
    }

    // Returns true if at least three slider handles are visible on the page.
    public boolean areAllSlidersDisplayed() {
        return allSliderHandles.stream().filter(this::isVisible).count() >= 3;
    }

    // Returns a pipe-joined string of the tenure tick markers (used to detect scale changes).
    public String tenureScaleSignature() {
        StringBuilder sb = new StringBuilder();
        for (WebElement m : tenureScaleTicks)
            sb.append(m.getText().trim()).append('|');
        return sb.toString();
    }

    // Flips the tenure unit to months.
    public LoanCalculatorPage switchTenureToMonths() {
        click(tenureMonthsBtn);
        return this;
    }

    // Flips the tenure unit to years.
    public LoanCalculatorPage switchTenureToYears() {
        click(tenureYearsBtn);
        return this;
    }

    // Runs the shared UI validation (text boxes + sliders) used on every sub-tab.
    public UIValidationResult validateInputs() {
        UIValidationResult r = new UIValidationResult();
        r.loanAmountEnabled = isLoanAmountTextBoxEnabled();
        r.interestEnabled = isInterestTextBoxEnabled();
        r.tenureEnabled = isTenureTextBoxEnabled();
        r.emiEnabled = isEmiTextBoxEnabled();
        r.slidersVisible = areAllSlidersDisplayed();
        return r;
    }

    // Value object returned by validateInputs(); carries the enabled/visible state of every input control.
    public static class UIValidationResult {
        public boolean loanAmountEnabled, interestEnabled, tenureEnabled, emiEnabled, slidersVisible;

        // Compact debug string for log lines.
        @Override
        public String toString() {
            return String.format("amount=%s, interest=%s, tenure=%s, emi=%s, sliders=%s",
                    loanAmountEnabled, interestEnabled, tenureEnabled, emiEnabled, slidersVisible);
        }
    }
}
