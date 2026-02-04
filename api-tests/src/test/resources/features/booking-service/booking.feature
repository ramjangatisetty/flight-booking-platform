@booking
Feature: Booking Service API

  @smoke @id=BOOKING-001
  Scenario: API docs should be accessible
    Given I am testing the "booking" service
    When I call "GET" "/v3/api-docs"
    Then the response status should be 200

  @happyPath @id=BOOKING-010
  Scenario: Create a booking successfully
    Given I am testing the "booking" service
    And I ensure a correlation id header is present
    And I set an idempotency key header
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I capture "bookingId" as "bookingId"
    And the response json "status" should equal "PENDING_PAYMENT"

  @happyPath @id=BOOKING-011
  Scenario: Retrieve a booking by ID
    Given I am testing the "booking" service
    And I ensure a correlation id header is present
    And I set an idempotency key header
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I capture "bookingId" as "bookingId"
    When I call "GET" "/bookings/{bookingId}"
    Then the response status should be 200
    And the response json "bookingId" should equal "{bookingId}"

  @happyPath @id=BOOKING-012
  Scenario: Get booking status
    Given I am testing the "booking" service
    And I ensure a correlation id header is present
    And I set an idempotency key header
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I capture "bookingId" as "bookingId"
    When I call "GET" "/bookings/{bookingId}/status"
    Then the response status should be 200
    And the response json "bookingId" should equal "{bookingId}"
    And the response should contain "status"

  @happyPath @id=BOOKING-013
  Scenario: Get loyalty accrual for booking
    Given I am testing the "booking" service
    And I ensure a correlation id header is present
    And I set an idempotency key header
    And I have a valid CreateBookingRequest JSON payload
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 201
    And I capture "bookingId" as "bookingId"
    When I call "GET" "/bookings/{bookingId}/loyalty"
    Then the response status should be 200
    And the response json "bookingId" should equal "{bookingId}"

  @negative @id=BOOKING-020
  Scenario: Get non-existent booking returns 404
    Given I am testing the "booking" service
    When I call "GET" "/bookings/00000000-0000-0000-0000-000000000000"
    Then the response status should be 404

  @negative @id=BOOKING-021
  Scenario: Create booking with invalid seatClass returns 400
    Given I am testing the "booking" service
    And I have a CreateBookingRequest with flightId "FL123" and seatClass "INVALID"
    When I call "POST" "/bookings" with JSON body
    Then the response status should be 400
