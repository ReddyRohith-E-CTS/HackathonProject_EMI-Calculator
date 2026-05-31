@LoanCalculator @UI
Feature: Loan Calculator UI validations (Problem Statement 3)
  Sub-tab to validate for each TC lives in testdata/TestData.xlsx sheet "LoanCalculator".

  Background:
    Given the Loan Calculator page is open

  Scenario Outline: <tc> - UI validation
    Given I have loan calculator test data for "<tc>"
    When I select the sub-tab from the test data
    Then all visible input fields should be enabled
    And all sliders should be displayed

    Examples:
      | tc   |
      | TC07 |
      | TC09 |
      | TC10 |

  @TC08
  Scenario: TC08 - Tenure unit toggle changes the slider scale
    Given I have loan calculator test data for "TC08"
    When I select the sub-tab from the test data
    And I capture the tenure scale signature
    And I switch the tenure unit to "Mo"
    Then the tenure scale signature should change
    When I switch the tenure unit to "Yr"
    Then the tenure scale signature should match the original
