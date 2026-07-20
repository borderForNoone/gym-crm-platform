package com.gym.crm.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.client.JmsQueueClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class WorkloadSteps {
    private static final String TRAINER_USERNAME = "workloadTrainerUsername";
    private static final String TRAINING_YEAR = "workloadTrainingYear";
    private static final String TRAINING_MONTH = "workloadTrainingMonth";
    private static final String WORKLOAD_QUEUE = "trainer-workload-queue";
    private static final int TRAINING_DURATION = 45;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TestContext context;
    private final ApiClient workloadClient = new ApiClient(TestProperties.workloadBaseUrl());
    private final ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());
    private final JmsQueueClient jmsClient = new JmsQueueClient(TestProperties.jmsBrokerUrl(), TestProperties.jmsUser(), TestProperties.jmsPassword());

    @When("trainer workload message is sent")
    public void trainerWorkloadMessageIsSent() {
        LocalDate trainingDate = LocalDate.now();
        String trainerUsername = context.getString("trainerUsername");

        context.put(TRAINER_USERNAME, trainerUsername);
        context.put(TRAINING_YEAR, trainingDate.getYear());
        context.put(TRAINING_MONTH, trainingDate.getMonthValue());

        jmsClient.sendText(WORKLOAD_QUEUE, buildWorkloadMessage(trainerUsername, trainingDate, TRAINING_DURATION),
                Map.of("_type", "TrainerWorkloadRequest"));
    }

    @When("invalid trainer workload message is sent")
    public void invalidTrainerWorkloadMessageIsSent() {
        LocalDate today = LocalDate.now();

        jmsClient.sendText(WORKLOAD_QUEUE, buildInvalidWorkloadMessage(today), Map.of("_type", "TrainerWorkloadRequest"));
    }

    @When("trainer monthly workload is requested through workload service")
    public void trainerMonthlyWorkloadIsRequestedThroughWorkloadService() {
        context.setLastResponse(getTrainerWorkload(context.getString(TRAINER_USERNAME), context.getString(TRAINING_YEAR), context.getString(TRAINING_MONTH)));
    }

    @When("trainer workload is requested without authorization")
    public void trainerWorkloadIsRequestedWithoutAuthorization() {
        LocalDate now = LocalDate.now();

        context.setLastResponse(workloadClient.get("/trainer-workloads/system.trainer", null,
                Map.of("year", now.getYear(), "month", now.getMonthValue())));
    }

    @When("missing trainer monthly workload is requested through workload service")
    public void missingTrainerMonthlyWorkloadIsRequestedThroughWorkloadService() {
        LocalDate now = LocalDate.now();

        context.setLastResponse(workloadClient.get("/trainer-workloads/missing.trainer", context.getToken(), Map.of("year", now.getYear(),
                "month", now.getMonthValue())));
    }

    @Then("workload message is processed")
    public void workloadMessageIsProcessed() {
        assertTrainerDurationEventuallyEquals(TRAINING_DURATION);
    }

    @Then("workload response contains duration {int}")
    public void workloadResponseContainsDuration(int duration) {
        assertThat(context.getLastResponse().asString()).contains(String.valueOf(duration));
    }

    @Then("workload message is moved to DLQ")
    public void workloadMessageIsMovedToDlq() {
        String trainerUsername = context.getString("trainerUsername");
        LocalDate now = LocalDate.now();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Response response = workloadClient.get("/trainer-workloads/" + trainerUsername, context.getToken(), Map.of("year", now.getYear(),
                            "month", now.getMonthValue()));

                    assertThat(response.statusCode()).isEqualTo(404);
                });
    }

    @Then("workload service eventually contains trainer duration {int}")
    public void workloadServiceEventuallyContainsTrainerDuration(int duration) {
        assertTrainerDurationEventuallyEquals(duration);
    }

    @Then("workload service eventually does not contain trainer workload")
    public void workloadServiceEventuallyDoesNotContainTrainerWorkload() {
        String trainerUsername = context.getString("trainerUsername");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> assertTrainerWorkloadNotFound(trainerUsername));
    }

    private void assertTrainerDurationEventuallyEquals(int expectedDuration) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Response response = getTrainerWorkload(
                            context.getString("trainerUsername"),
                            LocalDate.now().getYear(),
                            LocalDate.now().getMonthValue());

                    assertThat(response.statusCode()).isEqualTo(200);
                    int actualDuration = Integer.parseInt(response.asString().trim());
                    assertThat(actualDuration).isEqualTo(expectedDuration);
                });
    }

    private Response getTrainerWorkload(String trainerUsername, Object year, Object month) {
        return workloadClient.get("/trainer-workloads/" + trainerUsername, context.getToken(), Map.of("year", year, "month", month));
    }

    private void assertTrainerWorkloadNotFound(String trainerUsername) {
        LocalDate now = LocalDate.now();

        Response response = getTrainerWorkload(trainerUsername, now.getYear(), now.getMonthValue());

        assertThat(response.statusCode()).isEqualTo(404);
    }

    private String buildWorkloadMessage(String trainerUsername, LocalDate trainingDate, int duration) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("trainerUsername", trainerUsername);
        message.put("trainerFirstName", "System");
        message.put("trainerLastName", "Trainer");
        message.put("isActive", true);
        message.put("trainingDate", trainingDate.toString());
        message.put("trainingDuration", duration);
        message.put("actionType", "ADD");

        return toJson(message);
    }

    private String buildInvalidWorkloadMessage(LocalDate trainingDate) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("trainerUsername", "");
        message.put("trainerFirstName", "Tom");
        message.put("trainerLastName", "Tomas");
        message.put("isActive", true);
        message.put("trainingDate", trainingDate.toString());
        message.put("trainingDuration", TRAINING_DURATION);
        message.put("actionType", "ADD");

        return toJson(message);
    }

    private String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize workload message", e);
        }
    }
}