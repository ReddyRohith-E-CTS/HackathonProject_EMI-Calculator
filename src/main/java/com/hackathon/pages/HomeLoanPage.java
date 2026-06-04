package com.hackathon.pages;

import com.hackathon.config.ConfigReader;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

// Page Object for the Home Loan EMI Calculator page.
// Provides navigation, data entry, and extraction of the yearly amortisation schedule.
public class HomeLoanPage extends BasePage {

    @FindBy(id = "menu-item-dropdown-2696")
    private WebElement loanCalcMenu;
    @FindBy(linkText = "Home Loan EMI Calculator")
    private WebElement homeLoanMenuLink;

    @FindAll({ @FindBy(id = "homeloanamount"), @FindBy(name = "loanamount") })
    private WebElement loanAmount;
    @FindAll({ @FindBy(id = "homeloaninterest"), @FindBy(name = "loaninterest") })
    private WebElement loanInterest;
    @FindAll({ @FindBy(id = "homeloanterm"), @FindBy(name = "loanterm") })
    private WebElement loanTenure;

    @FindBy(css = "tr.yearlypaymentdetails")
    private List<WebElement> yearlyRows;

    // Opens the dedicated Home Loan calculator page via the top menu.
    public HomeLoanPage openViaMenu(HomePage homePage) {
        actions().moveToElement(loanCalcMenu).perform();
        click(loanCalcMenu);
        wait.until(ExpectedConditions.visibilityOf(homeLoanMenuLink));
        click(homeLoanMenuLink);
        waitForReady();
        return this;
    }

    // Opens the dedicated Home Loan calculator page directly via its URL.
    public HomeLoanPage openDirect() {
        super.open(ConfigReader.get().get("home.loan.url"));
        return this;
    }

    // Fills the loan amount, interest rate and tenure inputs.
    public HomeLoanPage fillForm(String amount, String rate, String tenureYears) {
        typeReplacing(loanAmount, amount);
        typeReplacing(loanInterest, rate);
        typeReplacing(loanTenure, tenureYears);
        return this;
    }

    // Reads every row of the year-on-year schedule and returns header + data grid.
    public List<List<String>> extractYearlySchedule() {
        scrollBy(0, 800);
        wait.until(d -> !yearlyRows.isEmpty());
        if (yearlyRows.isEmpty())
            throw new IllegalStateException("Yearly schedule table is empty");

        List<List<String>> grid = new ArrayList<>();
        grid.add(List.of("Year", "Principal", "Interest", "Total Payment", "Balance", "Loan Paid To Date"));

        for (WebElement row : yearlyRows) {
            @SuppressWarnings("unchecked")
            List<String> cells = (List<String>) js(
                    "return Array.from(arguments[0].querySelectorAll('td')).map(td => td.innerText.trim());",
                    row);
            if (cells.size() < 5)
                continue;
            grid.add(List.of(cells.get(0), cells.get(1), cells.get(2),
                    cells.size() > 3 ? cells.get(3) : "",
                    cells.size() > 4 ? cells.get(4) : "",
                    cells.size() > 5 ? cells.get(5) : ""));
        }
        return grid;
    }
}
