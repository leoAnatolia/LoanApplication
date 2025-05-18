
# Loan Management API

## Overview
This is a Spring Boot-based Loan Management API that allows creating, listing, and paying loans with advanced features like installment management and credit limit tracking.

## Features
- Create loans for customers
- List loans by customer
- List loan installments
- Pay loan installments with early payment discounts and late payment penalties
- Credit limit management
- Secure REST API with basic authentication

## Prerequisites
- Java 17+
- Maven

## Configuration
- Database: H2 in-memory database
- Authentication: HTTP Basic Authentication
    - Default Admin Credentials:
        - Username: admin
        - Password: adminPass

## API Endpoints

### Create Loan
`POST /v1/loans/{customerId}`
- Parameters(as JSON at request body):
    - `customerId`: ID of the customer
    - `loanAmount`: Total loan amount
    - `numberOfInstallments`: 6, 9, 12, or 24
    - `interestRate`: Between 0.1 and 0.5
- example:
```json
{
    "customerId": 1,
    "loanAmount": 10000,
    "numberOfInstallments": 12,
    "interestRate": 0.2
}
```

### List Customer Loans
`GET /v1/loans/{customerId}`

### List Loan Installments
`GET /v1/loans/{loanId}/installments`

### Pay Loan
`POST /v1/loans/{loanId}/pay`
- Parameters:
    - `paymentAmount`: Amount to pay

## Loan Creation Rules
- Must have sufficient credit limit
- Installments: 6, 9, 12, or 24 months
- Interest rate: 0.1 - 0.5
- All installments have equal amount
- First installment due on first day of next month

## Payment Rules
- Pay whole installments only
- Earliest unpaid installments paid first
- Early payment: Discount of 0.1% per day before due date
- Late payment: Penalty of 0.1% per day after due date
- Cannot pay installments more than 3 months ahead

## Running the Application
```bash
# Clone the repository
git clone [repository-url]

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

## Testing
- Use Postman or curl to interact with the API
- Authenticate with provided admin credentials

## Error Handling
- Comprehensive error messages for:
    - Insufficient credit limit
    - Invalid loan parameters
    - Payment validation errors

## Monitoring
- Check H2 console for database interactions
- Configure logging in `application.properties`
