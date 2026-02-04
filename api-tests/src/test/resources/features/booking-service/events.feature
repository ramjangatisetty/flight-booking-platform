@booking @kafka @events
Feature: Booking Service Kafka Events

  Verify that the booking service publishes correct Kafka events after API operations.
  Events follow the EventEnvelope structure with meta and data sections.

  Background:
    Given I am testing the "booking" service
    And I am subscribed to Kafka topics:
      | inventory.reserve.requested.v1 |
      | booking.confirmed.v1           |
      | booking.rejected.v1            |

  @id=BOOKING-EVENT-001
  Scenario: Inventory reserve requested event published after booking creation
    Given I ensure a correlation id header is present
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I should receive a Kafka event with type "inventory.reserve.requested" within 10 seconds
    And the event correlationId should match the request correlationId
    And the event data should contain field "bookingId"

  @id=BOOKING-EVENT-002
  Scenario: Event envelope contains required metadata fields
    Given I ensure a correlation id header is present
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I should receive a Kafka event with type "inventory.reserve.requested" within 10 seconds
    And the event meta should contain field "eventId"
    And the event meta should contain field "eventType"
    And the event meta should contain field "eventVersion"
    And the event meta should contain field "occurredAt"
    And the event meta should contain field "correlationId"
    And the event meta should contain field "producer"
    And the event meta "producer" should equal "booking-service"

  @id=BOOKING-EVENT-003
  Scenario: Event data contains booking details
    Given I ensure a correlation id header is present
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I capture "bookingId" as "bookingId"
    And I should receive a Kafka event with type "inventory.reserve.requested" within 10 seconds
    And the event data should contain field "bookingId"
    And the event data should contain field "flightId"
    And the event data should contain field "seatClass"

  @id=BOOKING-EVENT-004
  Scenario: Each event has unique eventId
    Given I ensure a correlation id header is present
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I should receive a Kafka event with type "inventory.reserve.requested" within 10 seconds
    And I capture event meta "eventId" as "firstEventId"
    And the event meta should contain field "eventId"
