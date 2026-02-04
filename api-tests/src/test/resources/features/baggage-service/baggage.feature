@baggage @xmlRest
Feature: Baggage Service XML API

  @smoke @id=BAGGAGE-001
  Scenario: Health endpoint should be accessible
    Given I am testing the "baggage" service
    When I call "GET" "/actuator/health"
    Then the response status should be 200

  @happyPath @id=BAGGAGE-010
  Scenario: Check in baggage successfully
    Given I am testing the "baggage" service
    And I have a valid BaggageCheckinRequest XML payload
    When I send XML request to "/baggage/checkin"
    Then the response status should be 200
    And the response should be valid XML
    And I capture XML element "bagTag" as "bagTag"
