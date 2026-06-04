# EMI Calculator Automation Project - User Guide

> **Problem Statement:** Find the **interest amount for current year** for a car loan and display **first-month interest + principal split**.  
> **System Under Test:** [emicalculator.net](https://emicalculator.net/)  
> **Automation Stack:** Selenium + Cucumber (BDD) + TestNG + Maven + Apache POI

---

## 1) Objective

Automate and validate the following:

1. **Car Loan EMI flow**
   - Car price: **₹15,00,000**
   - Interest rate: **9.5% p.a.**
   - Tenure: **1 year (12 months)**
   - Display and verify:
     - EMI
     - First-month interest amount
     - First-month principal amount

2. **Home Loan flow**
   - Navigate from menu to **Home Loan EMI Calculator**
   - Fill input fields
   - Extract **year-on-year table**
   - Save table data to Excel

3. **Loan Calculator UI flow**
   - Navigate from menu to **Loan Calculator**
   - Validate text boxes and slider scales in:
     - EMI Calculator
     - Loan Amount Calculator
     - Loan Tenure Calculator
   - Change tenure unit (Year/Month) and validate slider scale updates
   - Reuse common UI validation methods across all 3 tabs

---

## 2) Quick Business Example (Car Loan)

For:

- Principal $P = 1{,}500{,}000$
- Annual interest rate $R = 9.5\%$
- Monthly rate $r = \frac{R}{12}$
- Tenure $n = 12$

EMI formula:

$$
EMI = \frac{P \cdot r \cdot (1+r)^n}{(1+r)^n - 1}
$$

Expected (approx):

- **EMI:** ₹1,31,524
- **First-month interest:** ₹11,875
- **First-month principal:** ₹1,19,649

---

## 3) Key Automation Scope Covered

- Validation of financial calculations and displayed transactions
- Table extraction and Excel persistence
- Form filling and multiple UI validations
- Menu-based navigation
- Reusable methods using POM utilities
- Scrolling and dynamic element handling

---

## 4) Detailed Project Structure

```
HackathonProject_EMI-Calculator/
│
├── pom.xml                              # Maven build configuration
│                                         # Dependencies: Selenium, Cucumber, TestNG, POI, Log4j2, AssertJ
│                                         # Plugins: compiler, surefire, allure-maven, maven-jar
│
├── testng.xml                           # TestNG parallel execution config
│                                         # Defines Chrome + Edge test threads
│                                         # parallel="tests" for browser parallelization
│
├── README.md                            # This file
│
├── src/
│   │
│   ├── main/java/com/hackathon/
│   │   │
│   │   ├── base/
│   │   │   └── BaseClass.java           # Multi-browser driver management
│   │   │                                 # ThreadLocal<WebDriver> driver
│   │   │                                 # Implicit + page load timeouts
│   │   │                                 # Browser launch/close lifecycle
│   │   │
│   │   ├── config/
│   │   │   └── ConfigReader.java        # Properties file loader (singleton)
│   │   │                                 # Base URL, timeouts, headless flag
│   │   │                                 # Report paths, log configuration
│   │   │
│   │   ├── pages/                       # POM - Page Object Model
│   │   │   ├── BasePage.java            # Base page helper methods
│   │   │   │                             # click(), typeReplacing(), text()
│   │   │   │                             # scrollIntoView(), wait helpers
│   │   │   │
│   │   │   ├── HomePage.java            # Main EMI Calculator page
│   │   │   │                             # Car Loan, Home Loan, Loan Calculator tabs
│   │   │   │                             # Locators: loanAmount, loanInterest, loanTerm
│   │   │   │                             # Actions: selectTab(), enterLoanDetails()
│   │   │   │
│   │   │   ├── HomeLoanPage.java        # Dedicated Home Loan page
│   │   │   │                             # Year-on-year table extraction
│   │   │   │                             # Schedule table WebElement locators
│   │   │   │
│   │   │   └── LoanCalculatorPage.java  # Multi-tab Loan Calculator
│   │   │                                 # EMI Calculator, Loan Amount, Loan Tenure tabs
│   │   │                                 # Slider interactions and UI validations
│   │   │
│   │   ├── reportmanager/
│   │   │   └── ExtentManager.java       # Thread-safe Extent report driver
│   │   │                                 # Thread-local ExtentTest nodes
│   │   │                                 # startTest(), endTest(), flush()
│   │   │
│   │   └── utils/
│   │       ├── EMICalculatorUtil.java   # Financial calculations
│   │       │                             # EMI formula in pure Java
│   │       │                             # First-month split calculation
│   │       │                             # Currency parsing/formatting
│   │       │
│   │       ├── ExcelUtils.java          # Apache POI wrapper
│   │       │                             # Read test data: TestDataReader delegate
│   │       │                             # Write results: car loan summary, home loan schedule
│   │       │
│   │       ├── ScreenshotUtils.java     # Screenshot capture utility
│   │       │                             # OutputType.FILE + FileCopy
│   │       │                             # Timestamp + scenario name in filename
│   │       │
│   │       └── TestDataReader.java      # Excel test data cache + loader
│   │                                     # Loads TestData.xlsx on init
│   │                                     # Keyed by sheet name + TC id
│   │                                     # Returns Map<String, String> per row
│   │
│   └── test/
│       │
│       ├── java/com/hackathon/
│       │   │
│       │   ├── context/
│       │   │   └── ScenarioContext.java # Per-scenario shared state (PicoContainer DI)
│       │   │                             # Page objects: HomePage, HomeLoanPage, LoanCalcPage
│       │   │                             # SoftAssertions softly
│       │   │                             # Extracted data bags: put(k,v) / get(k)
│       │   │
│       │   ├── hooks/
│       │   │   └── Hooks.java           # Cucumber lifecycle hooks
│       │   │                             # @Before: initialize Extent test, clear cookies
│       │   │                             # @After: capture screenshots, softly.assertAll()
│       │   │
│       │   ├── listeners/
│       │   │   └── TestListener.java    # TestNG listeners (ISuiteListener, ITestListener)
│       │   │                             # onStart: wipe screenshots/ and allure-results/
│       │   │                             # onFinish: ExtentManager.flush()
│       │   │                             # Test event logging (onTestStart, onTestSuccess, etc.)
│       │   │
│       │   ├── runners/
│       │   │   └── TestRunner.java      # Single Cucumber-TestNG runner class
│       │   │                             # @CucumberOptions(features, glue, tags, plugins)
│       │   │                             # Extends BaseClass
│       │   │                             # Referenced in testng.xml twice (Chrome + Edge)
│       │   │
│       │   └── stepdefinitions/
│       │       ├── CarLoanSteps.java     # Car Loan feature steps
│       │       │                         # Given/When/Then for car loan EMI flow
│       │       ├── HomeLoanSteps.java    # Home Loan feature steps
│       │       │                         # Navigate, fill, extract table
│       │       └── LoanCalculatorSteps.java # UI validation steps
│       │                                   # Tab switching, slider interaction, scale check
│       │
│       └── resources/
│           │
│           ├── config.properties        # Framework configuration
│           │                             # base.url, implicit.wait, page.load.timeout
│           │                             # headless, output.path, report.path
│           │                             # No test data in properties (lives in Excel)
│           │
│           ├── extent.properties        # Extent report configuration
│           │                             # report.path, report.theme, report.title
│           │
│           ├── allure.properties        # Allure report configuration
│           │                             # allure.results.directory = target/allure-results
│           │
│           ├── log4j2.xml               # Log4j2 logging configuration
│           │                             # Console + RollingFile appender
│           │                             # logs/automation.log (daily rollover, 10 MB max)
│           │
│           ├── features/                # Cucumber feature files (Gherkin)
│           │   ├── CarLoanEMI.feature           # TC01-TC04: Car loan validation
│           │   ├── HomeLoanYearlySchedule.feature # TC05-TC06: Home loan table extract
│           │   └── LoanCalculatorUI.feature       # TC07-TC10: UI checks + reusable validation
│           │
│           └── testdata/
│               └── TestData.xlsx        # Master test data workbook
│                                         # Sheets: CarLoan, HomeLoan, LoanCalculator
│                                         # Columns: TestCaseID, LoanAmount, Rate, Tenure, etc.
│                                         # Loaded once at runtime by TestDataReader
│
├── target/                              # Maven build output (gitignored)
│   ├── classes/                         # Compiled .class files
│   ├── test-classes/
│   ├── allure-results/                  # Allure raw JSON results
│   ├── surefire-reports/                # TestNG/JUnit XML reports
│   └── allure-report/                   # Generated Allure HTML (after mvn allure:report)
│
├── reports/                             # Test report outputs (gitignored)
│   ├── extent/                          # Extent HTML reports (timestamped)
│   │   └── ExtentReport_<yyyy.MM.dd_HH.mm.ss>.html
│   └── cucumber/                        # Cucumber HTML + JSON
│       ├── cucumber.html
│       └── cucumber.json
│
├── output/                              # Generated Excel exports (gitignored)
│   ├── CarLoan_EMI_Summary.xlsx         # Car loan data export
│   └── HomeLoan_YearlySchedule.xlsx     # Home loan amortization table export
│
├── screenshots/                         # Per-scenario screenshot captures (gitignored)
│   └── TCxx_<scenario-slug>_<PASS|FAIL>_<browser>_<yyyyMMdd_HHmmss_SSS>.png
│
├── logs/                                # Log files (gitignored)
│   └── automation.log                   # Rolling log (daily + 10 MB size rollover)
│
└── .gitignore                           # Excludes target/, reports/, output/, etc.
```

### Folder Descriptions

| Folder                                         | Purpose                                                        |
| ---------------------------------------------- | -------------------------------------------------------------- |
| `src/main/java/com/hackathon/base/`            | WebDriver initialization, lifecycle, ThreadLocal management    |
| `src/main/java/com/hackathon/config/`          | Properties loading, centralized configuration                  |
| `src/main/java/com/hackathon/pages/`           | Page Object Model - UI element locators & page actions         |
| `src/main/java/com/hackathon/reportmanager/`   | Thread-safe Extent report driver                               |
| `src/main/java/com/hackathon/utils/`           | EMI math, Excel I/O, screenshots, test data                    |
| `src/test/java/com/hackathon/context/`         | Scenario-scoped shared state & soft assertions (PicoContainer) |
| `src/test/java/com/hackathon/hooks/`           | Cucumber @Before / @After lifecycle hooks                      |
| `src/test/java/com/hackathon/listeners/`       | TestNG suite & test event listeners                            |
| `src/test/java/com/hackathon/runners/`         | TestRunner - the single Cucumber runner                        |
| `src/test/java/com/hackathon/stepdefinitions/` | Step definitions - keyword implementations                     |
| `src/test/resources/features/`                 | Gherkin feature files (BDD scenarios)                          |
| `src/test/resources/testdata/`                 | Master Excel workbook with all test inputs                     |
| `target/`                                      | Maven build artifacts (compiled code, reports)                 |
| `reports/`                                     | Web-browsable HTML test reports                                |
| `output/`                                      | Excel export artefacts from test execution                     |
| `screenshots/`                                 | Per-scenario .png captures for documentation                   |
| `logs/`                                        | Rolling application logs                                       |

---

---

## 5) Tech Stack

- **Java 17**
- **Maven**
- **Selenium 4**
- **Cucumber 7 + TestNG**
- **Apache POI** (Excel read/write)
- **AssertJ Soft Assertions**
- **Log4j2**
- **Extent / Cucumber / Allure reports**

---

## 6) Test Coverage Summary

### Car Loan EMI

- Verify EMI against formula
- Verify first-month interest
- Verify first-month principal
- Export summary to Excel

### Home Loan

- Navigate via menu
- Extract year-on-year amortization table
- Write extracted rows to Excel

### Loan Calculator UI

- Validate textbox editability and values
- Validate slider interactions and scale
- Validate Year vs Month tenure scale change
- Reuse validations in 3 calculator sub-tabs

---

## 7) Prerequisites

- Java 17 installed
- Maven installed
- Chrome and Edge installed
- Internet access to `emicalculator.net`

---

## 8) How to Run the Project

### 8.1) Command Line (Maven)

#### Full Suite (All Scenarios on Chrome + Edge in Parallel)

```powershell
mvn clean test
```

- Runs 10 scenarios × 2 browsers = **20 tests**
- Execution time: ~10-15 minutes
- Generates reports in `reports/`, Excel in `output/`

#### Full Suite with Headless Browsers

```powershell
mvn clean test -Dheadless=true
```

- No browser GUI (faster, CI/CD friendly)
- Output is the same - reports and screenshots captured

#### Run Only Car Loan Tests

```powershell
mvn clean test "-Dcucumber.filter.tags=@CarLoan"
```

- Runs TC01, TC02, TC03, TC04 on both browsers
- ~4 scenarios × 2 browsers = 8 tests

#### Run Only Home Loan Tests

```powershell
mvn clean test "-Dcucumber.filter.tags=@HomeLoan"
```

- Runs TC05, TC06 on both browsers
- ~2 scenarios × 2 browsers = 4 tests

#### Run Only UI Tests

```powershell
mvn clean test "-Dcucumber.filter.tags=@UI"
```

- Runs TC07, TC08, TC09, TC10 on both browsers
- ~4 scenarios × 2 browsers = 8 tests

#### Run Specific Test Case by Tag

```powershell
mvn clean test "-Dcucumber.filter.tags=@TC03"
mvn clean test "-Dcucumber.filter.tags=@TC05"
```

- Runs only that specific test case on both browsers

#### Run Smoke / Regression

```powershell
mvn clean test "-Dcucumber.filter.tags=@Smoke"
mvn clean test "-Dcucumber.filter.tags=@Regression"
```

#### Combine Tag Filters (OR Logic)

```powershell
mvn clean test "-Dcucumber.filter.tags=@CarLoan or @HomeLoan"
```

- Runs Car Loan AND Home Loan scenarios

#### Run with Custom Timeout (in seconds)

```powershell
mvn clean test -Dimplicit.wait=10 -Dpage.load.timeout=15
```

#### Compile Only (Skip Tests)

```powershell
mvn clean compile
```

- Compiles source without running tests

#### Clean Build

```powershell
mvn clean
```

- Removes all generated files (target/, logs/, screenshots/)

---

### 8.2) Report Generation

#### Generate Allure HTML Dashboard

```powershell
mvn allure:report
```

- Input: `target/allure-results/` (generated during test run)
- Output: `target/allure-report/index.html`
- Open in browser: File → Open File → select the HTML

#### Launch Allure Dashboard in Default Browser

```powershell
mvn allure:serve
```

- Generates and auto-opens Allure report in browser
- Press `Ctrl+C` to stop the server

#### Clean and Regenerate All Reports

```powershell
mvn clean test allure:report
```

- Clears all previous artefacts, runs tests, generates Allure report

---

### 8.3) From IDE (Eclipse / IntelliJ)

#### Run Full Suite via TestNG

1. Right-click on `testng.xml`
2. Select **Run As → TestNG Suite**
3. Two browsers open in parallel, 10 scenarios per browser
4. Results visible in Console tab
5. Reports generated in `reports/`

#### Run Single Feature File

1. Right-click on `.feature` file (e.g., `CarLoanEMI.feature`)
2. Select **Run As → Cucumber Feature**
3. Scenarios run on both browsers (configured in testng.xml)

#### Run Single Scenario

1. Right-click on a scenario line in `.feature` file
2. Select **Run As → Cucumber Scenario**

#### Debug Mode

1. Right-click `testng.xml` → **Debug As → TestNG Suite**
2. Set breakpoints in step definitions or pages
3. Execution pauses at breakpoints

#### Run TestRunner Class Directly

1. Right-click `TestRunner.java`
2. Select **Run As → TestNG Class**
3. Runs as a TestNG test (not via Cucumber runner)

---

### 8.4) Advanced Maven Commands

#### Run with Debug Output

```powershell
mvn clean test -X
```

- Verbose logging for troubleshooting

#### Run Single Test Class

```powershell
mvn test -Dtest=TestRunner
```

#### Skip Tests During Build

```powershell
mvn clean install -DskipTests
```

- Builds JAR without running tests

#### Run Tests in Fail-Safe (Continue on Failure)

```powershell
mvn clean verify
```

- Runs tests via maven-failsafe-plugin (testng.xml still used)

#### Force Re-download Dependencies

```powershell
mvn clean test -U
```

- Useful if dependencies are corrupted

#### Install to Local Maven Repository

```powershell
mvn clean install
```

- Builds and installs project JAR locally

---

### 8.5) Continuous Integration (Jenkins)

#### Basic Jenkins Pipeline

```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        stage('Build') {
            steps { sh 'mvn clean compile' }
        }
        stage('Test') {
            steps { sh 'mvn test -Dheadless=true' }
        }
        stage('Reports') {
            steps {
                sh 'mvn allure:report'
                publishHTML([
                    reportDir: 'reports/extent',
                    reportFiles: '*.html',
                    reportName: 'Extent Report'
                ])
            }
        }
    }
    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'output/*.xlsx, screenshots/*.png, logs/*.log'
        }
    }
}
```

#### Jenkins with Parameterized Build

```groovy
parameters {
    choice(name: 'TAGS', choices: ['@Smoke', '@Regression', '@UI', '@CarLoan', ''])
    booleanParam(name: 'HEADLESS', defaultValue: true)
}
stages {
    stage('Test') {
        steps {
            sh "mvn clean test -Dheadless=${HEADLESS} -Dcucumber.filter.tags=${TAGS}"
        }
    }
}
```

---

### 8.6) Docker Execution (Optional)

#### Dockerfile Example

```dockerfile
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY . .
RUN mvn clean compile
CMD ["mvn", "test", "-Dheadless=true"]
```

#### Build and Run

```bash
docker build -t emi-calculator-tests .
docker run emi-calculator-tests
```

---

### 8.7) Execution Methods Quick Reference

| Method                    | Command                                          | Best For              | Time      |
| ------------------------- | ------------------------------------------------ | --------------------- | --------- |
| **Full Suite (Headed)**   | `mvn clean test`                                 | Local development     | 10-15 min |
| **Full Suite (Headless)** | `mvn clean test -Dheadless=true`                 | CI/CD pipelines       | 8-12 min  |
| **Car Loan Only**         | `mvn clean test -Dcucumber.filter.tags=@CarLoan` | Feature validation    | 2-3 min   |
| **Specific TC**           | `mvn clean test -Dcucumber.filter.tags=@TC03`    | Bug reproduction      | 10-15 sec |
| **IDE TestNG**            | Right-click `testng.xml` → Run                   | Interactive debugging | 10-15 min |
| **IDE Feature**           | Right-click `.feature` → Run                     | Feature development   | 5-7 min   |
| **Debug Mode**            | Right-click `testng.xml` → Debug                 | Troubleshooting       | varies    |
| **Allure Report**         | `mvn allure:report`                              | Report generation     | 2-3 min   |
| **Compile Only**          | `mvn clean compile`                              | Check build health    | 1-2 min   |
| **Jenkins Pipeline**      | Declarative pipeline                             | Automated CI/CD       | 15-20 min |
| **Docker**                | `docker run emi-calculator-tests`                | Containerized CI/CD   | 20-25 min |
