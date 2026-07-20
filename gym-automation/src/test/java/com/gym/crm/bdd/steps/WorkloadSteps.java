package com.gym.crm.bdd.steps;

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
    private static final String DLQ_QUEUE = "trainer-workload-dlq";
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

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("trainerUsername", trainerUsername);
        message.put("trainerFirstName", "System");
        message.put("trainerLastName", "Trainer");
        message.put("isActive", true);
        message.put("trainingDate", trainingDate.toString());
        message.put("trainingDuration", 45);
        message.put("actionType", "ADD");

        try {
            String json = OBJECT_MAPPER.writeValueAsString(message);
            jmsClient.sendText(WORKLOAD_QUEUE, json, Map.of("_type", "TrainerWorkloadRequest"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @When("invalid trainer workload message is sent")
    public void invalidTrainerWorkloadMessageIsSent() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("trainerUsername", "");
        body.put("trainerFirstName", "John");
        body.put("trainerLastName", "Doe");
        body.put("isActive", true);
        body.put("trainingDate", LocalDate.now().toString());
        body.put("trainingDuration", 45);
        body.put("actionType", "ADD");

        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build invalid workload message payload", e);
        }

        jmsClient.sendText(WORKLOAD_QUEUE, json, Map.of("_type", "TrainerWorkloadRequest"));
    }

    @When("trainer monthly workload is requested through workload service")
    public void trainerMonthlyWorkloadIsRequestedThroughWorkloadService() {
        context.setLastResponse(workloadClient.get("/trainer-workloads/" + context.getString(TRAINER_USERNAME), context.getToken(),
                Map.of("year", context.getString(TRAINING_YEAR), "month", context.getString(TRAINING_MONTH))));
    }

    @When("trainer workload is requested without authorization")
    public void trainerWorkloadIsRequestedWithoutAuthorization() {
        LocalDate now = LocalDate.now();

        context.setLastResponse(workloadClient.get("/trainer-workloads/system.trainer", null, Map.of("year", now.getYear(),
                "month", now.getMonthValue())));
    }

    @Then("workload message is processed")
    public void workloadMessageIsProcessed() {
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    String username = context.getString(TRAINER_USERNAME);
                    String path = "/trainer-workloads/" + username;
                    Map<String, Object> params = Map.of("year", context.getString(TRAINING_YEAR),
                            "month", context.getString(TRAINING_MONTH));

                    Response response = workloadClient.get(path, context.getToken(), params);

                    assertThat(response.statusCode()).isEqualTo(200);
                    int actualDuration = Integer.parseInt(response.asString().trim());
                    assertThat(actualDuration).isEqualTo(45);
                });
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
                    var response = workloadClient.get("/trainer-workloads/" + trainerUsername, context.getToken(), Map.of("year", now.getYear(),
                            "month", now.getMonthValue()));

                    assertThat(response.statusCode()).isEqualTo(404);
                });
    }

    @Then("workload service eventually contains trainer duration {int}")
    public void workloadServiceEventuallyContainsTrainerDuration(int duration) {
        String trainerUsername = context.getString("trainerUsername");
        LocalDate now = LocalDate.now();

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Response response = workloadClient.get("/trainer-workloads/" + trainerUsername, context.getToken(),
                            Map.of("year", now.getYear(), "month", now.getMonthValue()));

                    assertThat(response.statusCode()).isEqualTo(200);
                    int actualDuration = Integer.parseInt(response.asString().trim());
                    assertThat(actualDuration).isEqualTo(duration);
                });
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

    private void assertTrainerWorkloadNotFound(String trainerUsername) {
        var response = workloadClient.get("/trainer-workloads/" + trainerUsername, context.getToken(), Map.of("year", LocalDate.now().getYear(), "month", LocalDate.now().getMonthValue()));

        assertThat(response.statusCode()).isEqualTo(404);
    }
}