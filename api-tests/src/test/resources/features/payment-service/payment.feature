@payment
Feature: Payment Service API

  @smoke @id=PAYMENT-001
  Scenario: API docs should be accessible
    Given I am testing the "payment" service
    When I call "GET" "/v3/api-docs"
    Then the response status should be 200
