package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

        Response response = coreClient.post("/trainees/register", null, Payloads.trainee(uniqueFirstName("Auth"), uniqueLastName("User")));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        JsonPath json = response.jsonPath();
        context.put("authUsername", json.getString(USERNAME));
        context.put("authPassword", json.getString(PASSWORD));
    }


    @Given("gym user is authenticated")
    public void theGymUserIsAuthenticated() {
        String username = context.getString("authUsername");
        String password = context.getString("authPassword");

        if (username == null || password == null) {
            if (context.getString("trainerUsername") != null) {
                username = context.getString("trainerUsername");
                password = context.getString("trainerPassword");
            } else {
                username = context.getString("traineeUsername");
                password = context.getString("traineePassword");
            }
        }

        Response response = coreClient.post("/auth/login", null, Payloads.login(username, password));
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        context.setToken(response.jsonPath().getString("token"));
        assertThat(context.getToken()).isNotBlank();
    }

    @Given("trainer is authenticated")
    public void trainerIsAuthenticated() {
        String username = context.getString("trainerUsername");
        String password = context.getString("trainerPassword");
        Response response = coreClient.post("/auth/login", null, Payloads.login(username, password));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        context.setToken(response.jsonPath().getString("token"));
    }

    @Given("trainer and trainee are registered")
    public void trainerAndTraineeAreRegistered() {
        registerTraineeUser();
        registerTrainerUser();
    }

    @When("training is created through core service")
    public void trainingIsCreatedThroughCoreService() {
        String trainerUsername = context.getString("trainerUsername");
        String traineeUsername = context.getString("traineeUsername");

        Response response = coreClient.post("/trainings", context.getToken(), Payloads.training(traineeUsername, trainerUsername, 45));

        context.setLastResponse(response);
    }

    private void registerTrainee() {
        Response response = coreClient.post("/trainees/register", null, Payloads.trainee(uniqueFirstName("Trainee"), uniqueLastName("User")));

        context.setLastResponse(response);

        storeCredentials("trainee", response);
    }

    private void registerTraineeUser() {
        Response response = coreClient.post("/trainees/register", null, Payloads.trainee(uniqueFirstName("Trainee"), uniqueLastName("User")));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        JsonPath json = response.jsonPath();
        context.put("traineeUsername", json.getString(USERNAME));
        context.put("traineePassword", json.getString(PASSWORD));
    }

    private void registerTrainerUser() {
        Response response = coreClient.post("/trainers/register", null, Payloads.trainer(uniqueFirstName("Trainer"),
                        uniqueLastName("User"),
                        "Cardio"));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        JsonPath json = response.jsonPath();

        context.put("trainerUsername", json.getString(USERNAME));
        context.put("trainerPassword", json.getString(PASSWORD));
        System.out.println("REGISTERED TRAINER:");
        System.out.println("username = " + context.getString("trainerUsername"));
        System.out.println("password = " + context.getString("trainerPassword"));
    }


    private void storeCredentials(String prefix, Response response) {
        if (response.statusCode() != HttpStatus.OK.value()) {
            return;
        }

        JsonPath json = response.jsonPath();

        context.put(prefix + "Username", json.getString(USERNAME));
        context.put(prefix + "Password", json.getString(PASSWORD));
    }

    private String uniqueFirstName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private String uniqueLastName(String prefix) {
        return (prefix + (System.nanoTime() % 100000)).toLowerCase(Locale.ROOT);
    }
}