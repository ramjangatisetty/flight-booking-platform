@payment @kafka @events
Feature: Payment Service Kafka Events

  Verify that the payment service publishes correct Kafka events after processing payment requests.
  Events follow the EventEnvelope structure with meta and data sections.

  Background:
    Given I am testing the "payment" service
    And I am subscribed to Kafka topics:
      | payment.succeeded.v1 |
      | payment.failed.v1    |

  @id=PAYMENT-EVENT-001 @e2e
  Scenario: Payment succeeded event published after successful payment
    # This scenario requires the full saga flow:
    # 1. Seed inventory
    # 2. Create booking (triggers saga)
    # 3. Inventory reserved
    # 4. Payment requested
    # 5. Payment service processes and publishes payment.succeeded
    Given I am testing the "inventory" service
    And I ensure a correlation id header is present
    And I have a SeedInventoryRequest with flightId "FL-PAY-001" and seatClass "ECONOMY" and availableSeats 10
    When I call "POST" "/inventory/admin/seed" with JSON body
    Then the response status should be 200
    # Create booking to trigger full saga
    Given I am testing the "booking" service
    And I have a CreateBookingRequest with flightId "FL-PAY-001" and seatClass "ECONOMY"
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I should receive a Kafka event with type "payment.succeeded" within 30 seconds
    And the event correlationId should match the request correlationId
    And the event data should contain field "paymentId"
    And the event data should contain field "bookingId"
