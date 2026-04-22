Feature: Movement API Integration Tests

  Background:
    * url accountBaseUrl

  Scenario: Full movement lifecycle - deposit, withdrawal, insufficient balance, daily limit
    # Setup: create account with 2000 balance (accountNumber is auto-generated)
    Given path '/api/cuentas'
    And request { "accountType": "Savings", "initialBalance": 2000.00, "active": true, "customerId": "CLI-001" }
    When method post
    Then status 201
    * def accountNum = response.data.accountNumber

    # Create withdrawal of -575
    Given path '/api/movimientos'
    And request { "accountNumber": "#(accountNum)", "value": -575.00 }
    When method post
    Then status 201
    And match response.data.movementType == 'Withdrawal'
    And match response.data.balance == 1425.00
    * def movementId = response.data.movementId

    # Create deposit of +100
    Given path '/api/movimientos'
    And request { "accountNumber": "#(accountNum)", "value": 100.00 }
    When method post
    Then status 201
    And match response.data.movementType == 'Deposit'
    And match response.data.balance == 1525.00

    # Get movement by ID
    Given path '/api/movimientos', movementId
    When method get
    Then status 200
    And match response.data.movementId == movementId

    # List all movements
    Given path '/api/movimientos'
    When method get
    Then status 200
    And match response.data != '#[0]'

    # Reject insufficient balance
    Given path '/api/movimientos'
    And request { "accountNumber": "#(accountNum)", "value": -99999.00 }
    When method post
    Then status 400
    And match response.message == 'Saldo no disponible'

    # Verify account balance unchanged
    Given path '/api/cuentas', accountNum
    When method get
    Then status 200
    And match response.data.availableBalance == 1525.00

  Scenario: Delete movement reverses balance
    Given path '/api/cuentas'
    And request { "accountType": "Savings", "initialBalance": 1000.00, "active": true, "customerId": "CLI-001" }
    When method post
    Then status 201
    * def acct = response.data.accountNumber

    Given path '/api/movimientos'
    And request { "accountNumber": "#(acct)", "value": -200.00 }
    When method post
    Then status 201
    * def mvId = response.data.movementId

    # Delete the movement
    Given path '/api/movimientos', mvId
    When method delete
    Then status 200

    # Balance should be restored to 1000
    Given path '/api/cuentas', acct
    When method get
    Then status 200
    And match response.data.availableBalance == 1000.00

  Scenario: Movement on non-existent account returns 404
    Given path '/api/movimientos'
    And request { "accountNumber": "NONEXISTENT-999", "value": 100.00 }
    When method post
    Then status 404

