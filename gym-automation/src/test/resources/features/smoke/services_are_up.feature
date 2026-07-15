@component @core
Feature: Gym core service component API

  @trainee-register
  Scenario: Register trainee successfully
    When a trainee is registered through core service
    Then the response status is 200
    And the response contains generated credentials

  @auth-login
  Scenario: Reject invalid login credentials
    When the user logs in with invalid credentials
    Then the response status is 401
    And the response contains an error body

  @training-types
  Scenario: Reject forbidden training types request
    When training types are requested without authorization
    Then the response status is 403