package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkloadSteps {
    private static final String TRAINER_USERNAME = "workloadTrainerUsername";
    private static final String TRAINING_YEAR = "workloadTrainingYear";
    private static final String TRAINING_MONTH = "workloadTrainingMonth";

    private final TestContext context;
    private final ApiClient workloadClient;

    public WorkloadSteps(TestContext context) {
        this.context = context;
        this.workloadClient = new ApiClient(TestProperties.workloadBaseUrl());
    }

    @When("trainer workload is updated through workload service")
    public void trainerWorkloadIsUpdatedThroughWorkloadService() {
        String trainerUsername = uniqueTrainerUsername();
        LocalDate trainingDate = LocalDate.now();

        context.put(TRAINER_USERNAME, trainerUsername);
        context.put(TRAINING_YEAR, trainingDate.getYear());
        context.put(TRAINING_MONTH, trainingDate.getMonthValue());

        context.setLastResponse(workloadClient.put("/trainer-workloads", context.getToken(), Payloads.createWorkloadPayload(trainerUsername, 45)));
    }

    @When("trainer monthly workload is requested through workload service")
    public void trainerMonthlyWorkloadIsRequestedThroughWorkloadService() {
        context.setLastResponse(workloadClient.get("/trainer-workloads/" + context.getString(TRAINER_USERNAME), context.getToken(),
                Map.of("year", context.getString(TRAINING_YEAR), "month", context.getString(TRAINING_MONTH))));
    }

    @When("invalid trainer workload is sent through workload service")
    public void invalidTrainerWorkloadIsSentThroughWorkloadService() {
        var response = workloadClient.put("/trainer-workloads", context.getToken(), Payloads.createWorkloadPayload("some.trainer", 0));

        context.setLastResponse(response);
    }

    @When("trainer workload is requested without authorization")
    public void trainerWorkloadIsRequestedWithoutAuthorization() {
        context.setLastResponse(workloadClient.get("/trainer-workloads/system.trainer", null, Map.of("year", LocalDate.now().getYear(), "month", LocalDate.now().getMonthValue())));
    }

    @Then("workload service eventually does not contain trainer workload")
    public void workloadServiceEventuallyDoesNotContainTrainerWorkload() {
        String trainerUsername = context.getString("trainerUsername");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> assertTrainerWorkloadNotFound(trainerUsername));
    }

    @When("missing trainer monthly workload is requested through workload service")
    public void missingTrainerMonthlyWorkloadIsRequestedThroughWorkloadService() {
        context.setLastResponse(workloadClient.get("/trainer-workloads/missing.trainer", context.getToken(), Map.of("year", LocalDate.now().getYear(), "month", LocalDate.now().getMonthValue())));
    }

    @Then("workload service eventually contains trainer duration {int}")
    public void workloadServiceEventuallyContainsTrainerDuration(int duration) {
        String trainerUsername = context.getString("trainerUsername");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> assertTrainerWorkloadDuration(trainerUsername, duration));
    }

    @Then("workload response contains duration {int}")
    public void theWorkloadResponseContainsDuration(int duration) {
        assertThat(context.getLastResponse().asString()).isEqualTo(String.valueOf(duration));
    }

    private String uniqueTrainerUsername() {
        return "system.trainer" + System.currentTimeMillis();
    }

    private void assertTrainerWorkloadDuration(String trainerUsername, int duration) {
        var response = workloadClient.get("/trainer-workloads/" + trainerUsername, context.getToken(), Map.of("year", LocalDate.now().getYear(), "month", LocalDate.now().getMonthValue()));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).contains(String.valueOf(duration));
    }

    private void assertTrainerWorkloadNotFound(String trainerUsername) {
        var response = workloadClient.get("/trainer-workloads/" + trainerUsername, context.getToken(), Map.of("year", LocalDate.now().getYear(), "month", LocalDate.now().getMonthValue()));

        assertThat(response.statusCode()).isEqualTo(404);
    }
}