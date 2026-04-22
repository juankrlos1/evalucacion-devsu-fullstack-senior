Feature: Customer API Integration Tests

  Background:
    * url customerBaseUrl
    * def uniqueId = 'CLI-IT-' + java.lang.System.currentTimeMillis()

  Scenario: Create, read, update, patch and delete a customer
    # Create customer
    Given path '/api/clientes'
    And request
    """
    {
      "customerId": "#(uniqueId)",
      "name": "Jose Lema",
      "gender": "Male",
      "age": 32,
      "identification": "#(uniqueId + '-ID')",
      "address": "Otavalo sn y principal",
      "phone": "098254785",
      "password": "1234",
      "active": true
    }
    """
    When method post
    Then status 201
    And match response.success == true
    And match response.data.customerId == uniqueId
    And match response.data.name == 'Jose Lema'

    # Get customer by ID
    Given path '/api/clientes', uniqueId
    When method get
    Then status 200
    And match response.success == true
    And match response.data.customerId == uniqueId
    And match response.data.name == 'Jose Lema'
    And match response.data.active == true

    # List all customers
    Given path '/api/clientes'
    When method get
    Then status 200
    And match response.success == true
    And match response.data != '#[0]'

    # PUT - Replace customer
    Given path '/api/clientes', uniqueId
    And request
    """
    {
      "customerId": "#(uniqueId)",
      "name": "Jose Lema Updated",
      "gender": "Male",
      "age": 33,
      "identification": "#(uniqueId + '-ID')",
      "address": "Amazonas y NNUU",
      "phone": "097548965",
      "password": "5678",
      "active": true
    }
    """
    When method put
    Then status 200
    And match response.data.name == 'Jose Lema Updated'
    And match response.data.address == 'Amazonas y NNUU'

    # PATCH - Partial update
    Given path '/api/clientes', uniqueId
    And request { "phone": "099999999" }
    When method patch
    Then status 200
    And match response.data.phone == '099999999'
    And match response.data.name == 'Jose Lema Updated'

    # DELETE - Soft delete (deactivate)
    Given path '/api/clientes', uniqueId
    When method delete
    Then status 200

    # Verify deactivated
    Given path '/api/clientes', uniqueId
    When method get
    Then status 200
    And match response.data.active == false

  Scenario: Get non-existent customer returns 404
    Given path '/api/clientes/NON-EXISTENT-999'
    When method get
    Then status 404
    And match response.success == false

  Scenario: Create customer with duplicate ID returns conflict
    * def dupId = 'CLI-DUP-' + java.lang.System.currentTimeMillis()

    Given path '/api/clientes'
    And request
    """
    {
      "customerId": "#(dupId)",
      "name": "First Customer",
      "gender": "Male",
      "age": 25,
      "identification": "#(dupId + '-ID')",
      "address": "Address 1",
      "phone": "111111111",
      "password": "pass1",
      "active": true
    }
    """
    When method post
    Then status 201

    # Try to create with same ID
    Given path '/api/clientes'
    And request
    """
    {
      "customerId": "#(dupId)",
      "name": "Duplicate Customer",
      "gender": "Female",
      "age": 30,
      "identification": "#(dupId + '-ID2')",
      "address": "Address 2",
      "phone": "222222222",
      "password": "pass2",
      "active": true
    }
    """
    When method post
    Then status 409
    And match response.success == false

  Scenario: Validation - missing required fields returns 400
    Given path '/api/clientes'
    And request { "customerId": "", "name": "" }
    When method post
    Then status 400

