@inventory @kafka @events
Feature: Inventory Service Kafka Events

  Verify that the inventory service publishes correct Kafka events in response to reservation requests.
  Events follow the EventEnvelope structure with meta and data sections.

  Background:
    Given I am testing the "inventory" service
    And I am subscribed to Kafka topics:
      | inventory.reserved.v1  |
      | inventory.rejected.v1  |
      | inventory.released.v1  |

  @id=INVENTORY-EVENT-001 @e2e
  Scenario: Inventory reserved event published after successful reservation
    # This scenario requires the full saga flow:
    # 1. Seed inventory for a flight
    # 2. Create booking (triggers inventory.reserve.requested)
    # 3. Inventory service reserves and publishes inventory.reserved
    Given I am testing the "inventory" service
    And I ensure a correlation id header is present
    # Seed inventory first
    Given I have a SeedInventoryRequest with flightId "FL-EVENT-001" and seatClass "ECONOMY" and availableSeats 10
    When I call "POST" "/inventory/admin/seed" with JSON body
    Then the response status should be 200
    # Now create booking via booking service to trigger the saga
    Given I am testing the "booking" service
    And I have a CreateBookingRequest with flightId "FL-EVENT-001" and seatClass "ECONOMY"
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I should receive a Kafka event with type "inventory.reserved" within 15 seconds
    And the event correlationId should match the request correlationId
    And the event data should contain field "reservationId"
    And the event data should contain field "bookingId"

  @id=INVENTORY-EVENT-002 @e2e
  Scenario: Inventory rejected event published when no inventory available
    # Create booking for non-existent flight (no inventory seeded)
    Given I am testing the "booking" service
    And I ensure a correlation id header is present
    And I have a CreateBookingRequest with flightId "FL-NO-INVENTORY" and seatClass "ECONOMY"
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I should receive a Kafka event with type "inventory.rejected" within 15 seconds
    And the event correlationId should match the request correlationId
    And the event data should contain field "reason"
