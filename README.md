# Insurance Coordination of Benefits (COB)

A Java 21 project implementing insurance Coordination of Benefits rules to determine the order of responsibility across multiple insurance plans and calculate how benefits are coordinated for a claim.

## Features

### 1. Coordination of Benefits
- Determine primary, secondary, and subsequent payer order.
- Apply birthday and dependent coverage rules where applicable.
- Account for active-employee and other coverage-ordering rules.
- Support court-order considerations.
- Handle effective dates and changes in coverage.
- Support coordination across three or more insurance plans.

### 2. Payment Strategies
- Support standard coordination strategies.
- Support non-duplication of benefits.
- Support carve-out payment approaches.
- Apply configured payment limits and patient-responsibility calculations.
- Reconcile payer payments while avoiding duplicate reimbursement under the configured rules.

### 3. Claim Denials and Coverage Changes
- Represent claim denial scenarios.
- Account for coverage changes and applicable effective dates.
- Apply the configured coordination rules when payer information changes.

### 4. Medicare Coordination
- Include Medicare-related coordination thresholds and rule handling.
- Apply configured Medicare coordination logic to determine payer responsibility.

**Important:** Insurance and Medicare rules can change and may depend on plan terms, jurisdiction, and individual circumstances. The implemented rules are for this project's educational and demonstration purposes and should be validated against current authoritative requirements before production use.

## Technology Stack

- Java 21
- Apache Maven
- JUnit testing
- Git and GitHub

## Project Structure

```text
insurance-coordination-of-benefits/
├── src/
│   ├── main/java/
│   └── test/java/
├── pom.xml
└── README.md
```

The project contains 23 main Java source files and 9 test files, according to the previously recorded project inventory.

## Prerequisites

- JDK 21
- Maven Wrapper or an installed Maven distribution, depending on the project configuration

## Getting Started

### Clone the repository

```bash
git clone https://github.com/sunnykotha/insurance-coordination-of-benefits.git
cd insurance-coordination-of-benefits
```

### Run tests

From the project directory, run the Maven Wrapper if `mvnw.cmd` is present:

```powershell
.\mvnw.cmd test
```

If the project uses an installed Maven distribution instead, run:

```powershell
mvn test
```

### Package the project

Using the Maven Wrapper:

```powershell
.\mvnw.cmd package
```

Or using installed Maven:

```powershell
mvn package
```

The previously recorded package artifact was:

`target/insurance-coordination-of-benefits-1.0.0.jar`

## Test Coverage

The recorded test suite covered the project's implemented coordination and payment rules. The last recorded successful verification reported:

- Tests: 65
- Failures: 0
- Errors: 0
- Skipped: 0

These are the previously recorded results; rerun the tests to verify the current checkout.

## Learning Objectives

This project demonstrates:

- Java object-oriented design
- Insurance payer-order determination
- Business-rule and effective-date validation
- Coordination across multiple insurance plans
- Payment strategy and claim-denial handling
- Medicare coordination logic
- Automated testing with JUnit
- Maven build and packaging workflows
- Git and GitHub version control

## Repository

https://github.com/sunnykotha/insurance-coordination-of-benefits

## Disclaimer

This project is intended for educational and demonstration purposes. It is not an official insurance adjudication platform and does not replace payer contracts, official Medicare guidance, applicable laws, or professional review. Validate all rules against current authoritative sources before using them in a real insurance workflow.