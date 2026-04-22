Feature: Account API Integration Tests

  Background:
    * url accountBaseUrl

  Scenario: Create, read, update, patch and deactivate an account
    # Create account (accountNumber is auto-generated)
    Given path '/api/cuentas'
    And request
    """
    {
      "accountType": "Savings",
      "initialBalance": 2000.00,
      "active": true,
      "customerId": "CLI-001"
    }
    """
    When method post
    Then status 201
    And match response.success == true
    And match response.data.accountNumber == '#notnull'
    And match response.data.availableBalance == 2000.00
    * def accountNum = response.data.accountNumber

    # Get account
    Given path '/api/cuentas', accountNum
    When method get
    Then status 200
    And match response.data.accountNumber == accountNum
    And match response.data.accountType == 'Savings'

    # List accounts
    Given path '/api/cuentas'
    When method get
    Then status 200
    And match response.data != '#[0]'

    # PUT - Replace account
    Given path '/api/cuentas', accountNum
    And request
    """
    {
      "accountType": "Checking",
      "initialBalance": 3000.00,
      "active": true,
      "customerId": "CLI-001"
    }
    """
    When method put
    Then status 200
    And match response.data.accountType == 'Checking'

    # PATCH - Partial update
    Given path '/api/cuentas', accountNum
    And request { "accountType": "Savings" }
    When method patch
    Then status 200
    And match response.data.accountType == 'Savings'

    # DELETE - Deactivate
    Given path '/api/cuentas', accountNum
    When method delete
    Then status 200

  Scenario: Get non-existent account returns 404
    Given path '/api/cuentas/NONEXISTENT-999'
    When method get
    Then status 404
    And match response.success == false

  Scenario: Create account with missing required fields returns 400
    Given path '/api/cuentas'
    And request { "accountType": "Savings" }
    When method post
    Then status 400
    And match response.success == false

