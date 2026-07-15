@component @core
Feature: Gym core service component API

  @trainee-register
  Scenario: Register trainee successfully
    When a trainee is registered through core service
    Then the response status is 200
    And the response contains generated credentials

  @auth-login
  Scenario: Reject login with invalid password
    Given a trainee is registered through core service
    When the user logs in with invalid password
    Then the response status is 401

  @training-types
  Scenario: Reject forbidden training types request
    When training types are requested without authorization
    Then the response status is 403