Feature: Report API Integration Tests

  Background:
    * url accountBaseUrl

  Scenario: Generate JSON account statement report
    # Setup: create account and movement (accountNumber is auto-generated)
    Given path '/api/cuentas'
    And request { "accountType": "Savings", "initialBalance": 2000.00, "active": true, "customerId": "CLI-001" }
    When method post
    Then status 201
    * def accountNum = response.data.accountNumber

    Given path '/api/movimientos'
    And request { "accountNumber": "#(accountNum)", "value": -575.00 }
    When method post
    Then status 201

    # Get JSON report
    Given path '/api/reportes'
    And param fechaInicio = '2020-01-01'
    And param fechaFin = '2030-12-31'
    And param cliente = 'CLI-001'
    And param format = 'json'
    When method get
    Then status 200
    And match response.success == true
    And match response.data != '#[0]'
    And match each response.data contains { customer: 'CLI-001' }

  Scenario: Generate PDF account statement report
    Given path '/api/reportes'
    And param fechaInicio = '2020-01-01'
    And param fechaFin = '2030-12-31'
    And param cliente = 'CLI-001'
    And param format = 'pdf'
    When method get
    Then status 200
    And match response.success == true
    And match response.data.pdf != null
    And match response.data.pdf != ''

  Scenario: Report for customer with no accounts returns empty
    Given path '/api/reportes'
    And param fechaInicio = '2020-01-01'
    And param fechaFin = '2020-12-31'
    And param cliente = 'NONEXISTENT'
    And param format = 'json'
    When method get
    Then status 200
    And match response.data == '#[0]'

