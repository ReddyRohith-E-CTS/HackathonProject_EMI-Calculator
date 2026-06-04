@HomeLoan @Regression
Feature: Home Loan EMI Calculator - year-on-year schedule extract (Problem Statement 2)
  Inputs live in testdata/TestData.xlsx sheet "HomeLoan".

  @TC05
  Scenario: TC05 - Navigate to Home Loan EMI Calculator via the top menu
    Given the EMI Calculator homepage is open
    Given I have home loan test data for "TC05"
    When I open the menu item from the test data
    Then the Home Loan EMI Calculator page should load

  @TC06
  Scenario: TC06 - Year-on-year schedule is extracted and stored in Excel
    Given I have home loan test data for "TC06"
    And the Home Loan EMI Calculator page is open
    When I fill the home loan form with values from the test data
    And I extract the year-on-year schedule
    Then the schedule should have the minimum yearly rows defined in the test data
    And the schedule is stored in the Home Loan Excel file
    And the Home Loan Excel file should exist on disk
