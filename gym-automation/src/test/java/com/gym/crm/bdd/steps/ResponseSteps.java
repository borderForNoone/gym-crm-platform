package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class ResponseSteps {
    private final TestContext context;

    @Then("response status is {int}")
    public void responseStatusIs(int status) {
        assertThat(context.getLastResponse().statusCode()).isEqualTo(status);
    }

    @Then("response contains generated credentials")
    public void responseContainsGeneratedCredentials() {
        assertThat(context.getLastResponse().jsonPath().getString("username")).isNotBlank();
        assertThat(context.getLastResponse().jsonPath().getString("password")).isNotBlank();
    }

    @Then("response contains error body")
    @Then("response contains an error body")
    public void responseContainsAnErrorBody() {
        assertThat(context.getLastResponse().jsonPath().getString("errorMessage")).isNotBlank();
    }
}