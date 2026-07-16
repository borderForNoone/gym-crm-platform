package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class CoreSteps {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private final TestContext context;
    private final ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());

    @When("trainee is registered through core service")
    public void traineeIsRegisteredThroughCoreService() {
        registerTrainee();
    }

    @When("training types are requested without authorization")
    public void trainingTypesAreRequestedWithoutAuthorization() {
        Response response = coreClient.get("/trainings/types", null, Map.of());

        context.setLastResponse(response);
    }

    private void registerTrainee() {
        Response response = coreClient.post("/trainees/register", null, Payloads.trainee(uniqueName("Trainee"), uniqueName("User")));

        context.setLastResponse(response);
        storeCredentials("trainee", response);
    }

    private void storeCredentials(String prefix, Response response) {
        if (response.statusCode() != HttpStatus.OK.value()) {
            return;
        }

        JsonPath json = response.jsonPath();

        context.put(prefix + "Username", json.getString(USERNAME));
        context.put(prefix + "Password", json.getString(PASSWORD));
    }

    private String uniqueName(String prefix) {
        return (prefix + System.nanoTime()).toLowerCase(Locale.ROOT);
    }
}