package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
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

    @When("a trainee is registered through core service")
    public void aTraineeIsRegisteredThroughCoreService() {
        Response response = coreClient.post("/trainees/register", null, Payloads.trainee(uniqueName("Trainee"), uniqueName("User")));

        context.setLastResponse(response);
        rememberCredentials("trainee", response);
    }

    @When("training types are requested without authorization")
    public void trainingTypesAreRequestedWithoutAuthorization() {
        context.setLastResponse(coreClient.get("/trainings/types", null, Map.of()));
    }

    private void rememberCredentials(String prefix, Response response) {
        if (response.statusCode() == 200) {
            context.put(prefix + "Username", response.jsonPath().getString("username"));
            context.put(prefix + "Password", response.jsonPath().getString("password"));
        }
    }

    private String uniqueName(String prefix) {
        return (prefix + System.nanoTime()).toLowerCase(Locale.ROOT);
    }
}