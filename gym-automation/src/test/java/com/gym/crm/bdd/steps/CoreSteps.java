package com.gym.crm.bdd.steps;

import com.gym.crm.bdd.client.ApiClient;
import com.gym.crm.bdd.config.TestProperties;
import com.gym.crm.bdd.support.Payloads;
import com.gym.crm.bdd.support.TestContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
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
    public void authenticatedGymUser() {
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

        login(username, password);
    }

    @Given("trainer is authenticated")
    public void trainerIsAuthenticated() {
        String username = context.getString("trainerUsername");
        String password = context.getString("trainerPassword");

        assertThat(username).as("Trainer username exists").isNotBlank();
        assertThat(password).as("Trainer password exists").isNotBlank();

        login(username, password);
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

        assertThat(context.getToken()).as("JWT token should exist").isNotBlank();
        Response response = coreClient.post("/trainings", context.getToken(), Payloads.training(traineeUsername, trainerUsername, 45));
        context.setLastResponse(response);
    }

    @When("trainee is registered through core service with details")
    public void traineeIsRegisteredThroughCoreServiceWithDetails(Map<String, String> details) {
        Response response = coreClient.post("/trainees/register", null, Payloads.createTraineePayload(withUniqueLastName(details)));

        context.setLastResponse(response);

        JsonPath json = response.jsonPath();
        context.put("traineeUsername", json.getString(USERNAME));
        context.put("traineePassword", json.getString(PASSWORD));
    }

    @When("trainer is registered through core service with details")
    public void trainerIsRegisteredThroughCoreServiceWithDetails(Map<String, String> details) {
        Map<String, String> unique = withUniqueLastName(details);

        Response response = coreClient.post("/trainers/register", null, Map.of("firstName", unique.get("firstName"),
                "lastName", unique.get("lastName"), "specialization", unique.get("specialization")));
        context.setLastResponse(response);

        JsonPath json = response.jsonPath();
        context.put("trainerUsername", json.getString(USERNAME));
        context.put("trainerPassword", json.getString(PASSWORD));
    }

    @When("training is created through core service with details")
    public void trainingIsCreatedThroughCoreServiceWithDetails(Map<String,String> details) {
        String trainingDate = details.get("trainingDate");

        if ("today".equalsIgnoreCase(trainingDate)) {
            trainingDate = LocalDate.now().toString();
        }

        assertThat(context.getToken()).as("JWT token should exist").isNotBlank();
        Response response = coreClient.post("/trainings", context.getToken(), Map.of("traineeUsername", context.getString("traineeUsername"),
                "trainerUsername", context.getString("trainerUsername"), "trainingName", details.get("trainingName"),
                "trainingDate", trainingDate, "trainingDuration", Integer.parseInt(details.get("trainingDuration"))));
        context.setLastResponse(response);
    }

    @When("invalid training is created through core service with details")
    public void invalidTrainingIsCreatedThroughCoreServiceWithDetails(DataTable table) {
        Map<String, String> details = table.asMap(String.class, String.class);

        context.setLastResponse(coreClient.post("/trainings", context.getToken(), Payloads.createTrainingPayload(context.getString("traineeUsername"),
                context.getString("trainerUsername"), details)));
    }

    @When("trainee is deleted through core service")
    public void traineeIsDeletedThroughCoreService() {
        Response response = coreClient.delete("/trainees/" + context.getString("traineeUsername"), context.getToken());

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
        storeCredentials("trainee", response);
    }

    private void registerTrainerUser() {
        Response response = coreClient.post("/trainers/register", null, Payloads.trainer(uniqueFirstName("Trainer"), uniqueLastName("User"),
                "Cardio"));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        storeCredentials("trainer", response);
    }

    private void storeCredentials(String prefix, Response response) {
        JsonPath json = response.jsonPath();

        context.put(prefix + "Username", json.getString(USERNAME));
        context.put(prefix + "Password", json.getString(PASSWORD));
    }

    private Map<String, String> withUniqueLastName(Map<String, String> details) {
        Map<String, String> copy = new java.util.HashMap<>(details);
        String uniqueLastName = details.get("lastName") + (System.nanoTime() % 100000);

        copy.put("lastName", uniqueLastName);

        return copy;
    }

    private String uniqueFirstName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private String uniqueLastName(String prefix) {
        return (prefix + (System.nanoTime() % 100000)).toLowerCase(Locale.ROOT);
    }

    private void login(String username, String password) {
        assertThat(username).as("Username should exist").isNotBlank();
        assertThat(password).as("Password should exist").isNotBlank();

        Response response = coreClient.post("/auth/login", null, Payloads.login(username, password));
        context.setLastResponse(response);

        assertThat(response.statusCode()).as("Login should return 200. Response: %s", response.asString()).isEqualTo(HttpStatus.OK.value());
        String token = response.jsonPath().getString("token");
        assertThat(token).as("JWT token should exist").isNotBlank();
        context.setToken(token);
    }
}