package com.gym.crm.bdd.support;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.gym.crm.bdd.support.PayloadField.ADDRESS;
import static com.gym.crm.bdd.support.PayloadField.DATE_OF_BIRTH;
import static com.gym.crm.bdd.support.PayloadField.FIRST_NAME;
import static com.gym.crm.bdd.support.PayloadField.LAST_NAME;

@UtilityClass
public final class Payloads {
    public static Map<String, Object> createTraineePayload(Map<String, String> details) {
        return Map.of(FIRST_NAME.getValue(), details.get(FIRST_NAME.getValue()),
                LAST_NAME.getValue(), details.get(LAST_NAME.getValue()),
                DATE_OF_BIRTH.getValue(), details.get(DATE_OF_BIRTH.getValue()),
                ADDRESS.getValue(), details.get(ADDRESS.getValue()));
    }

    public static Map<String, Object> login(String username, String password) {
        return Map.of("username", username, "password", password);
    }

    public static Map<String, Object> trainee(String firstName, String lastName) {
        return Map.of("firstName", firstName, "lastName", lastName, "dateOfBirth", "2000-03-22", "address", "420 Oak St");
    }

    public static Map<String, Object> trainer(String firstName, String lastName, String specialization) {
        return Map.of("firstName", firstName, "lastName", lastName, "specialization", specialization);
    }

    public static Map<String, Object> training(String traineeUsername, String trainerUsername, int duration) {
        return Map.of("traineeUsername", traineeUsername,
                "trainerUsername", trainerUsername,
                "trainingName", "System Test Training",
                "trainingDate", LocalDate.now().toString(),
                "trainingDuration", duration);
    }

    public static Map<String, Object> createTrainingPayload(String traineeUsername, String trainerUsername, Map<String, String> details) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("traineeUsername", traineeUsername);
        payload.put("trainerUsername", trainerUsername);
        payload.put("trainingName", details.get("trainingName"));
        payload.put("trainingDate", parseTrainingDate(details.get("trainingDate")));
        payload.put("trainingDuration", parseDurationOrRaw(details.get("trainingDuration")));

        return payload;
    }

    private static Object parseTrainingDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (value.toLowerCase()) {
            case "today" -> LocalDate.now().toString();
            case "tomorrow" -> LocalDate.now().plusDays(1).toString();
            case "yesterday" -> LocalDate.now().minusDays(1).toString();
            default -> value;
        };
    }

    public static Map<String, Object> workload(String trainerUsername, int duration) {
        return Map.of("trainerUsername", trainerUsername,
                "trainerFirstName", "System",
                "trainerLastName", "Trainer",
                "isActive", true,
                "trainingDate", LocalDate.now().toString(),
                "trainingDuration", duration,
                "actionType", "ADD");
    }

    public static Map<String, Object> workloadWithoutUsername(int duration) {
        return Map.of("trainerFirstName", "System",
                "trainerLastName", "Trainer",
                "isActive", true,
                "trainingDate", LocalDate.now().toString(),
                "trainingDuration", duration,
                "actionType", "ADD");
    }

    public static Map<String, Object> invalidTraining() {
        return Map.of("trainerFirstName", "System",
                "trainerLastName", "Trainer",
                "isActive", true,
                "trainingDate", LocalDate.now().plusDays(1).toString(),
                "trainingDuration", 45,
                "actionType", "ADD"
        );
    }

    private static Object parseDurationOrRaw(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }
}