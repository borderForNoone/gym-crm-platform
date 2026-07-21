package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Locale;
import java.util.Map;

public class CoreSteps {
    private final TestContext context;
    private final ApiClient coreClient;

    public CoreSteps(TestContext context) {
        this.context = context;
        this.coreClient = new ApiClient(TestProperties.coreBaseUrl());
    }

    @When("trainee is registered through core service")
    public void aTraineeIsRegisteredThroughCoreService() {
        Response response = coreClient.post("/trainees/register", null, Payloads.createTraineePayload(uniqueName("Trainee"), uniqueName("User")));

        context.setLastResponse(response);
        rememberCredentials("trainee", response);
    }

    @When("invalid training is created through core service with details")
    public void invalidTrainingIsCreatedThroughCoreServiceWithDetails(DataTable table) {
        Map<String, String> details = table.asMap(String.class, String.class);

        context.setLastResponse(coreClient.post("/trainings", context.getToken(),
                Payloads.createTrainingPayload(context.getString("traineeUsername"), context.getString("trainerUsername"), details)));
    }

    @When("training types are requested without authorization")
    public void trainingTypesAreRequestedWithoutAuthorization() {
        context.setLastResponse(coreClient.get("/trainings/types", null, java.util.Map.of()));
    }

    @When("trainee is registered through core service with details")
    public void traineeIsRegisteredThroughCoreServiceWithDetails(DataTable table) {
        Response response = coreClient.post("/trainees/register", null, Payloads.createTraineePayload(details(table)));

        context.setLastResponse(response);
        rememberCredentials("trainee", response);
    }

    @When("trainer is registered through core service with details")
    public void trainerIsRegisteredThroughCoreServiceWithDetails(DataTable table) {
        Response response = coreClient.post("/trainers/register", null, Payloads.createTrainerPayload(details(table)));

        context.setLastResponse(response);
        rememberCredentials("trainer", response);
    }

    @When("training is created through core service with details")
    public void trainingIsCreatedThroughCoreServiceWithDetails(DataTable table) {
        context.setLastResponse(coreClient.post("/trainings", context.getToken(),
                Payloads.createTrainingPayload(context.getString("traineeUsername"), context.getString("trainerUsername"), details(table))));
    }

    @When("trainee is deleted through core service")
    public void traineeIsDeletedThroughCoreService() {
        context.setLastResponse(coreClient.delete("/trainees/" + context.getString("traineeUsername"), context.getToken()));
    }

    private Map<String, String> details(DataTable table) {
        return table.asMap(String.class, String.class);
    }

    private void rememberCredentials(String prefix, Response response) {
        if (response.statusCode() == 200) {
            context.put(String.format("%sUsername", prefix), response.jsonPath().getString("username"));
            context.put(String.format("%sPassword", prefix), response.jsonPath().getString("password"));
        }
    }

    private String uniqueName(String prefix) {
        return (prefix + System.nanoTime()).toLowerCase(Locale.ROOT);
    }
}