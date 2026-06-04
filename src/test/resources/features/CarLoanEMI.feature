@CarLoan @Smoke
Feature: Car Loan EMI calculation (Problem Statement 1)
  Inputs and verification type for each TC live in testdata/TestData.xlsx
  sheet "CarLoan" - feature files do not duplicate that data.

  Background:
    Given the EMI Calculator homepage is open

  Scenario Outline: <tc> - <description>
    Given I have car loan test data for "<tc>"
    When I select the Car Loan tab and enter the loan details from the test data
    Then the configured calculation should match the formula

    Examples:
      | tc   | description           |
      | TC01 | EMI value             |
      | TC02 | First month interest  |
      | TC03 | First month principal |

  @TC04
  Scenario: TC04 - Car loan EMI summary is exported to Excel
    Given I have car loan test data for "TC04"
    When I select the Car Loan tab and enter the loan details from the test data
    Then the car loan EMI summary is written to Excel
    And the Excel file should have at least 2 rows