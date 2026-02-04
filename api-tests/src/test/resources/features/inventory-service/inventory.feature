@inventory
Feature: Inventory Service API

  @smoke @id=INVENTORY-001
  Scenario: API docs should be accessible
    Given I am testing the "inventory" service
    When I call "GET" "/v3/api-docs"
    Then the response status should be 200

  @happyPath @id=INVENTORY-010
  Scenario: Seed inventory successfully
    Given I am testing the "inventory" service
    And I have a valid SeedInventoryRequest JSON payload
    When I call "POST" "/inventory/admin/seed" with JSON body
    Then the response status should be 200
    And the response should contain "flightId"
    And the response should contain "seatClass"
    And the response should contain "availableSeats"

  @happyPath @id=INVENTORY-011
  Scenario: Seed inventory with custom values
    Given I am testing the "inventory" service
    And I have a SeedInventoryRequest with flightId "FL-TEST-001" and seatClass "BUSINESS" and availableSeats 50
    When I call "POST" "/inventory/admin/seed" with JSON body
    Then the response status should be 200
    And the response json "flightId" should equal "FL-TEST-001"
    And the response json "seatClass" should equal "BUSINESS"

  @happyPath @id=INVENTORY-012
  Scenario: Reset inventory
    Given I am testing the "inventory" service
    When I call "POST" "/inventory/admin/reset" with empty body
    Then the response status should be 200

  @negative @id=INVENTORY-020
  Scenario: Get reservation by non-existent reservationId returns 404
    Given I am testing the "inventory" service
    When I call "GET" "/inventory/reservations/00000000-0000-0000-0000-000000000000"
    Then the response status should be 404

  @negative @id=INVENTORY-021
  Scenario: Get reservation by non-existent bookingId returns 404
    Given I am testing the "inventory" service
    When I call "GET" "/inventory/reservations/by-booking/00000000-0000-0000-0000-000000000000"
    Then the response status should be 404
