package com.hackathon.context;

import com.hackathon.pages.HomeLoanPage;
import com.hackathon.pages.HomePage;
import com.hackathon.pages.LoanCalculatorPage;
import org.assertj.core.api.SoftAssertions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Shared state bag injected by PicoContainer into every step-definition class per scenario.
// Holds page objects, captured UI data, soft assertions, and a generic key-value store.
public class ScenarioContext {

    // Active Car Loan / Home Loan homepage page object for the current scenario.
    public HomePage homePage;

    // Active Home Loan page object (populated during HomeLoan scenarios).
    public HomeLoanPage homeLoanPage;

    // Active Loan Calculator page object (populated during LoanCalculator scenarios).
    public LoanCalculatorPage loanCalculatorPage;

    // Yearly schedule rows extracted from the amortisation table (TC06).
    public List<List<String>> extractedSchedule;

    // Tenure scale signature captured before toggling the unit (TC08).
    public String tenureScaleSignatureBefore;

    // Tenure scale signature captured after toggling the unit (TC08).
    public String tenureScaleSignatureAfter;

    // Collects non-fatal assertion failures; flushed in Hooks.@After.
    public final SoftAssertions softly = new SoftAssertions();

    // Generic key-value store for arbitrary inter-step data sharing.
    private final Map<String, Object> bag = new HashMap<>();

    // Stores an arbitrary value under key for later retrieval by other step defs.
    public void put(String key, Object value) {
        bag.put(key, value);
    }

    // Retrieves a value previously stored via put(); cast to the call-site type.
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) bag.get(key);
    }
}
