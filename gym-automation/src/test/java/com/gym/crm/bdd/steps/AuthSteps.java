package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RequiredArgsConstructor
public class AuthSteps {
    private final TestContext context;
    private final ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());

    @When("user logs in with valid credentials")
    public void userLogsInWithValidCredentials() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.login(context.getString("traineeUsername"),
                context.getString("traineePassword"))));
    }

    @Given("registered trainer is authenticated")
    public void registeredTrainerIsAuthenticated() {
        String username = context.getString("trainerUsername");
        String password = context.getString("trainerPassword");

        assertThat(username).as("Trainer username should exist").isNotBlank();
        assertThat(password).as("Trainer password should exist").isNotBlank();

        Response response = coreClient.post("/auth/login", null, Payloads.login(username, password));
        context.setLastResponse(response);

        assertThat(response.statusCode()).as("Trainer login response: %s", response.asString()).isEqualTo(200);
        String token = response.jsonPath().getString("token");
        assertThat(token).isNotBlank();
        context.setToken(token);
    }

    @When("user logs in with invalid password")
    public void userLogsInWithInvalidPassword() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.login(context.getString("traineeUsername"), "wrong-password")));
    }

    @Given("registered trainee is authenticated")
    public void registeredTraineeIsAuthenticated() {
        Response response = coreClient.post("/auth/login", null, Payloads.login(context.getString("traineeUsername"),
                context.getString("traineePassword")));

        context.setLastResponse(response);
        context.setToken(response.jsonPath().getString("token"));
    }
}