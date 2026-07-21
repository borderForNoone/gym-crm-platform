package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AuthSteps {
    private static final String AUTH_USERNAME_KEY = "authUsername";
    private static final String WRONG_PASSWORD = "wrong-password";

    private final TestContext context;
    private final ApiClient coreClient;

    public AuthSteps(TestContext context) {
        this.context = context;
        this.coreClient = new ApiClient(TestProperties.coreBaseUrl());
    }

    @Given("authenticated gym user")
    public void anAuthenticatedGymUser() {
        aGymUserIsRegistered();
        theGymUserIsAuthenticated();
    }

    @Given("gym user is registered")
    public void aGymUserIsRegistered() {
        if (context.getString("authUsername") != null) {
            return;
        }

        Response response = coreClient.post("/trainees/register", null, Payloads.createTraineePayload(uniqueName("Auth"), uniqueName("User")));

        assertThat(response.statusCode()).isEqualTo(200);
        context.put("authUsername", response.jsonPath().getString("username"));
        context.put("authPassword", response.jsonPath().getString("password"));
    }

    @Given("gym user is authenticated")
    public void theGymUserIsAuthenticated() {
        authenticate(context.getString("authUsername"), context.getString("authPassword"));
    }

    @Given("registered trainee is authenticated")
    public void registeredTraineeIsAuthenticated() {
        authenticate(context.getString("traineeUsername"), context.getString("traineePassword"));
    }

    @Given("registered trainer is authenticated")
    public void registeredTrainerIsAuthenticated() {
        authenticate(context.getString("trainerUsername"), context.getString("trainerPassword"));
    }

    @When("user logs in with valid credentials")
    public void theUserLogsInWithValidCredentials() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.createLoginPayload(TestProperties.defaultUsername(), TestProperties.defaultPassword())));
    }

    @When("user logs in with invalid credentials")
    public void theUserLogsInWithInvalidCredentials() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.createLoginPayload("unknown.user", "wrong-password")));
    }

    @When("registered user logs in with wrong password")
    public void registeredUserLogsInWithWrongPassword() {
        context.setLastResponse(coreClient.post("/auth/login", null, Payloads.createLoginPayload(context.getString(AUTH_USERNAME_KEY), WRONG_PASSWORD)));
    }

    @Given("trainer is registered")
    public void trainerIsRegistered() {
        Response response = coreClient.post("/trainers/register", null, Payloads.createTrainerPayload(uniqueName("Trainer"), uniqueName("User"), "Yoga"));

        assertThat(response.statusCode()).isEqualTo(200);
        context.put("trainerUsername", response.jsonPath().getString("username"));
        context.put("trainerPassword", response.jsonPath().getString("password"));
    }

    @Given("trainer is authenticated")
    public void trainerIsAuthenticated() {
        registeredTrainerIsAuthenticated();
    }

    private String uniqueName(String prefix) {
        return (prefix + System.nanoTime()).toLowerCase(Locale.ROOT);
    }

    private void authenticate(String username, String password) {
        Response response = coreClient.post("/auth/login", null, Payloads.createLoginPayload(username, password));

        assertThat(response.statusCode()).isEqualTo(200);
        context.setToken(response.jsonPath().getString("token"));
        assertThat(context.getToken()).isNotBlank();
    }
}