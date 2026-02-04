@loyalty @soap
Feature: Loyalty Service SOAP API

  @smoke @id=LOYALTY-001
  Scenario: WSDL should be accessible
    Given I am testing the "loyalty" service
    When I call "GET" "/ws/loyalty.wsdl"
    Then the response status should be 200
    And the response should contain "definitions"

  @happyPath @id=LOYALTY-010
  Scenario: Enroll a new loyalty member
    Given I am testing the "loyalty" service
    And I have a valid EnrollMemberRequest SOAP request
    When I call SOAP operation "EnrollMember"
    Then the SOAP response should be successful
    And I capture SOAP element "memberId" as "memberId"

  @happyPath @id=LOYALTY-011
  Scenario: Get member status after enrollment
    Given I am testing the "loyalty" service
    And I have a valid EnrollMemberRequest SOAP request
    When I call SOAP operation "EnrollMember"
    Then the SOAP response should be successful
    And I capture SOAP element "memberId" as "memberId"
    Given I have a valid GetMemberStatusRequest SOAP request
    And I set SOAP element "memberId" to "{memberId}"
    When I call SOAP operation "GetMemberStatus"
    Then the SOAP response should be successful
