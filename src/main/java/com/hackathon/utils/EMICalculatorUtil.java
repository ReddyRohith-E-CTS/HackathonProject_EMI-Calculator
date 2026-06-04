package com.hackathon.utils;

public final class EMICalculatorUtil {

    private EMICalculatorUtil() {
    }

    // Converts an annual percentage rate to the per-month decimal rate used in EMI math.
    public static double monthlyRate(double annualRatePercent) {
        return annualRatePercent / 12.0 / 100.0;
    }

    // Computes EMI from principal, annual interest rate and tenure in months.
    public static double emi(double principal, double annualRatePercent, int months) {
        double r = monthlyRate(annualRatePercent);
        double pow = Math.pow(1 + r, months);
        return principal * r * pow / (pow - 1);
    }

    // Interest portion of the very first EMI = principal * monthly rate.
    public static double firstMonthInterest(double principal, double annualRatePercent) {
        return principal * monthlyRate(annualRatePercent);
    }

    // Principal portion of the very first EMI = EMI - first month interest.
    public static double firstMonthPrincipal(double principal, double annualRatePercent, int months) {
        return emi(principal, annualRatePercent, months) - firstMonthInterest(principal, annualRatePercent);
    }

    // Total interest paid over the full tenure = (EMI x months) - principal.
    public static double totalInterest(double principal, double annualRatePercent, int months) {
        return emi(principal, annualRatePercent, months) * months - principal;
    }

    // Strips rupee/comma formatting from a string and returns the rounded numeric
    // value.
    public static long parseIndianCurrency(String s) {
        if (s == null)
            throw new IllegalArgumentException("Currency string is null");
        String cleaned = s.replaceAll("[^0-9.\\-]", "").trim();
        if (cleaned.isEmpty())
            throw new IllegalArgumentException("No numeric content in: " + s);
        return Math.round(Double.parseDouble(cleaned));
    }
}
