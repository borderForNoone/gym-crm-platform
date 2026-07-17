@component @workload
Feature: Workload service component API

  @workload-update
  Scenario: Update trainer workload successfully
    Given trainer and trainee are registered
    And trainer is authenticated
    When trainer workload message is sent
    Then workload message is processed
    When trainer monthly workload is requested through workload service
    Then response status is 200
    And workload response contains duration 45

  @workload-get
  Scenario: Get trainer monthly workload successfully
    Given trainer and trainee are registered
    And gym user is authenticated
    When trainer workload message is sent
    Then workload message is processed
    When trainer monthly workload is requested through workload service
    Then response status is 200
    And workload response contains duration 45

  @workload-validation
  Scenario: Reject invalid workload message
    Given trainer and trainee are registered
    And gym user is authenticated
    When invalid trainer workload message is sent
    Then workload message is moved to DLQ

  @workload-edge-case @edge-case
    Scenario: Return not found for missing trainer workload
      Given gym user is registered
      And gym user is authenticated
      When missing trainer monthly workload is requested through workload service
      Then response status is 404
      And response contains error body

  @workload-security
  Scenario: Reject unauthorized workload request
    When trainer workload is requested without authorization
    Then response status is 403