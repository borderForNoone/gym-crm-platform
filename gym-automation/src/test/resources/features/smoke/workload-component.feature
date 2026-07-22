@component @workload
Feature: Workload service component API

  @workload-update
  Scenario: Update trainer workload successfully
    Given gym user is registered
    And gym user is authenticated
    When trainer workload is updated through workload service
    Then response status is 200

  @workload-get
  Scenario: Get trainer monthly workload successfully
    Given gym user is registered
    And gym user is authenticated
    When trainer workload is updated through workload service
    Then response status is 200
    When trainer monthly workload is requested through workload service
    Then response status is 200
    And workload response contains duration 45

  @workload-validation @exception-handling
  Scenario: Reject invalid workload update request
    Given trainer is registered
    And trainer is authenticated
    When invalid trainer workload is sent through workload service
    Then response status is 400
    And response contains error body

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