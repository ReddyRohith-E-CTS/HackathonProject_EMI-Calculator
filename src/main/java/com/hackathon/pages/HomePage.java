package com.hackathon.pages;

import com.hackathon.config.ConfigReader;
import com.hackathon.utils.EMICalculatorUtil;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

// Page Object for the EMI Calculator homepage (emicalculator.net).
// Covers the Car Loan tab and reads EMI, interest and principal from the results panel.
public class HomePage extends BasePage {

    @FindBy(id = "home-loan")
    private WebElement homeLoanTab;
    @FindBy(id = "personal-loan")
    private WebElement personalLoanTab;
    @FindBy(id = "car-loan")
    private WebElement carLoanTab;

    @FindBy(name = "loanamount")
    private WebElement loanAmount;
    @FindBy(name = "loaninterest")
    private WebElement loanInterest;
    @FindBy(name = "loanterm")
    private WebElement loanTerm;

    @FindBy(xpath = "//label[input[@id='loanyears']]")
    private WebElement tenureYears;
    @FindBy(xpath = "//label[input[@id='loanmonths']]")
    private WebElement tenureMonths;

    @FindBy(css = "#loanamountslider .ui-slider-handle")
    private WebElement loanAmountSlider;
    @FindBy(css = "#loaninterestslider .ui-slider-handle")
    private WebElement loanInterestSlider;
    @FindBy(css = "#loantermslider .ui-slider-handle")
    private WebElement loanTermSlider;

    @FindBy(xpath = "//div[@id='emiamount']//p/span")
    private WebElement emiAmount;
    @FindBy(xpath = "//div[@id='emitotalinterest']//p/span")
    private WebElement totalInterest;
    @FindBy(xpath = "//div[@id='emitotalamount']//p/span")
    private WebElement totalPayment;

    @FindBy(css = "tr.yearlypaymentdetails td.paymentyear")
    private List<WebElement> yearCells;

    public enum TenureUnit {
        YEARS, MONTHS
    }

    // Opens the EMI Calculator homepage URL from config.
    public HomePage open() {
        super.open(ConfigReader.get().get("base.url"));
        waitForReady();
        return this;
    }

    // Clicks the Car Loan tab and waits until it is active.
    public HomePage selectCarLoanTab() {
        click(carLoanTab);
        wait.until(ExpectedConditions.attributeContains(carLoanTab, "class", "active"));
        return this;
    }

    // Clicks the Home Loan tab and waits until it is active.
    public HomePage selectHomeLoanTab() {
        click(homeLoanTab);
        wait.until(ExpectedConditions.attributeContains(homeLoanTab, "class", "active"));
        return this;
    }

    // Clicks the Personal Loan tab and waits until it is active.
    public HomePage selectPersonalLoanTab() {
        click(personalLoanTab);
        wait.until(ExpectedConditions.attributeContains(personalLoanTab, "class", "active"));
        return this;
    }

    // Types the loan amount into the input field.
    public HomePage enterLoanAmount(String amount) {
        typeReplacing(loanAmount, amount);
        return this;
    }

    // Types the interest rate into the input field.
    public HomePage enterInterestRate(String rate) {
        typeReplacing(loanInterest, rate);
        return this;
    }

    // Selects Year/Month toggle and types the tenure value.
    public HomePage enterTenure(String tenure, TenureUnit unit) {
        click(unit == TenureUnit.YEARS ? tenureYears : tenureMonths);
        typeReplacing(loanTerm, tenure);
        return this;
    }

    // Reads the EMI tile value (rupees).
    public long readEmi() {
        return EMICalculatorUtil.parseIndianCurrency(text(emiAmount));
    }

    // Reads the Total Interest tile value (rupees).
    public long readTotalInterest() {
        return EMICalculatorUtil.parseIndianCurrency(text(totalInterest));
    }

    // Reads the Total Payment tile value (rupees).
    public long readTotalPayment() {
        return EMICalculatorUtil.parseIndianCurrency(text(totalPayment));
    }

    // Expands the year row and returns the first month's principal value.
    public long readFirstMonthPrincipal(int year) {
        expandYearRow(year);
        return EMICalculatorUtil.parseIndianCurrency((String) js(
                "var el = document.querySelector('#monthyear' + arguments[0] + " +
                        "        ' .monthlypaymentcontainer tbody tr:first-child td:nth-child(2)');" +
                        " return el ? el.innerText : '';",
                year));
    }

    // Expands the year row and returns the first month's interest value.
    public long readFirstMonthInterest(int year) {
        expandYearRow(year);
        return EMICalculatorUtil.parseIndianCurrency((String) js(
                "var el = document.querySelector('#monthyear' + arguments[0] + " +
                        "        ' .monthlypaymentcontainer tbody tr:first-child td:nth-child(3)');" +
                        " return el ? el.innerText : '';",
                year));
    }

    // Clicks the year row in the schedule to expand the monthly breakdown.
    private void expandYearRow(int year) {
        scrollBy(0, 800);
        WebElement row = findInListByText(yearCells, String.valueOf(year));
        scrollIntoView(row);
        String display = (String) js(
                "var el = document.querySelector('#monthyear' + arguments[0] + ' .monthlypaymentcontainer');" +
                        " return el ? getComputedStyle(el).display : 'none';",
                year);
        if ("none".equalsIgnoreCase(display))
            row.click();
        waitForJsTruthy(
                "(function(){var el=document.querySelector('#monthyear" + year + " .monthlypaymentcontainer');" +
                        " return el && getComputedStyle(el).display!=='none';})()");
    }

    // Returns true if the Loan Amount slider handle is visible.
    public boolean isLoanAmountSliderDisplayed() {
        return isVisible(loanAmountSlider);
    }

    // Returns true if the Interest Rate slider handle is visible.
    public boolean isLoanInterestSliderDisplayed() {
        return isVisible(loanInterestSlider);
    }

    // Returns true if the Loan Tenure slider handle is visible.
    public boolean isLoanTermSliderDisplayed() {
        return isVisible(loanTermSlider);
    }
}
