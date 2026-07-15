package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthSteps {
    private final TestContext context;
    private final ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());

    @When("the user logs in with valid credentials")
    public void theUserLogsInWithValidCredentials() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.login(context.getString("traineeUsername"),
                context.getString("traineePassword"))));
    }

    @When("the user logs in with invalid password")
    public void loginWithInvalidPassword() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.login(context.getString("traineeUsername"), "wrong-password")));
    }
}