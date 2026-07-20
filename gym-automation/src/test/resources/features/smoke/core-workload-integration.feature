@integration
Feature: Core and workload service integration

  @training-create @workload-get
  Scenario: Creating training updates trainer workload
    When trainee is registered through core service with details
      | firstName   | FlowTrainee |
      | lastName    | User        |
      | dateOfBirth | 2000-03-22  |
      | address     | 420 Oak St  |
    Then response status is 200
    When trainer is registered through core service with details
      | firstName      | FlowTrainer |
      | lastName       | User        |
      | specialization | Yoga        |
    Then response status is 200
    Given registered trainer is authenticated
    When training is created through core service with details
      | trainingName     | Integration Training |
      | trainingDate     | today                |
      | trainingDuration | 60                   |
    Then response status is 200
    And workload service eventually contains trainer duration 60

  @training-create @trainee-delete @workload-delete
  Scenario: Deleting trainee subtracts trainer workload
    When trainee is registered through core service with details
      | firstName   | FlowTrainee |
      | lastName    | DeleteUser  |
      | dateOfBirth | 2000-03-22  |
      | address     | 421 Oak St  |
    Then response status is 200
    When trainer is registered through core service with details
      | firstName      | FlowTrainer |
      | lastName       | DeleteUser  |
      | specialization | Yoga        |
    Then response status is 200
    Given registered trainer is authenticated
    When training is created through core service with details
      | trainingName     | Delete Flow Training |
      | trainingDate     | today                |
      | trainingDuration | 60                   |
    Then response status is 200
    And workload service eventually contains trainer duration 60
    When trainee is deleted through core service
    Then response status is 200
    And workload service eventually contains trainer duration 0

  @training-create @workload-not-updated @negative
    Scenario: Rejected training creation does not update trainer workload
      When trainee is registered through core service with details
        | firstName   | FlowTrainee  |
        | lastName    | NegativeUser |
        | dateOfBirth | 2000-03-22   |
        | address     | 422 Oak St   |
      Then response status is 200
      When trainer is registered through core service with details
        | firstName      | FlowTrainer  |
        | lastName       | NegativeUser |
        | specialization | Yoga         |
      Then response status is 200
      Given registered trainee is authenticated
      When invalid training is created through core service with details
        | trainingName     | Invalid Integration Training |
        | trainingDate     | today                        |
        | trainingDuration | 0                            |
      Then response status is 400
      And workload service eventually does not contain trainer workload