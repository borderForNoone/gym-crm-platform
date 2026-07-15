package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {
    private final TestContext context;
    private final ApiClient coreClient;

    public AuthSteps(TestContext context) {
        this.context = context;
        this.coreClient = new ApiClient(TestProperties.coreBaseUrl());
    }

    @Given("an authenticated gym user")
    public void anAuthenticatedGymUser() {
        Response response = coreClient.post("/auth/login", null, Payloads.login(TestProperties.defaultUsername(), TestProperties.defaultPassword()));

        assertThat(response.statusCode()).isEqualTo(200);
        context.setToken(response.jsonPath().getString("token"));
        assertThat(context.getToken()).isNotBlank();
    }

    @When("the user logs in with valid credentials")
    public void theUserLogsInWithValidCredentials() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.login(TestProperties.defaultUsername(), TestProperties.defaultPassword())));
    }

    @When("the user logs in with invalid credentials")
    public void theUserLogsInWithInvalidCredentials() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.login("unknown.user", "wrong-password")));
    }
}