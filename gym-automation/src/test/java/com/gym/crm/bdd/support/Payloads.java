package com.gym.crm.bdd.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static com.gym.crm.bdd.support.PayloadField.ACTION_TYPE;
import static com.gym.crm.bdd.support.PayloadField.ADDRESS;
import static com.gym.crm.bdd.support.PayloadField.DATE_OF_BIRTH;
import static com.gym.crm.bdd.support.PayloadField.FIRST_NAME;
import static com.gym.crm.bdd.support.PayloadField.IS_ACTIVE;
import static com.gym.crm.bdd.support.PayloadField.LAST_NAME;
import static com.gym.crm.bdd.support.PayloadField.PASSWORD;
import static com.gym.crm.bdd.support.PayloadField.SPECIALIZATION;
import static com.gym.crm.bdd.support.PayloadField.TRAINEE_USERNAME;
import static com.gym.crm.bdd.support.PayloadField.TRAINER_FIRST_NAME;
import static com.gym.crm.bdd.support.PayloadField.TRAINER_LAST_NAME;
import static com.gym.crm.bdd.support.PayloadField.TRAINER_USERNAME;
import static com.gym.crm.bdd.support.PayloadField.TRAINING_DATE;
import static com.gym.crm.bdd.support.PayloadField.TRAINING_DURATION;
import static com.gym.crm.bdd.support.PayloadField.TRAINING_NAME;
import static com.gym.crm.bdd.support.PayloadField.USERNAME;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Payloads {
    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Kyiv");
    private static final String DEFAULT_DATE_OF_BIRTH = "2000-03-22";
    private static final String DEFAULT_ADDRESS = "420 Oak St";
    private static final String DEFAULT_TRAINING_NAME = "System Test Training";
    private static final String DEFAULT_TRAINER_FIRST_NAME = "System";
    private static final String DEFAULT_TRAINER_LAST_NAME = "Trainer";
    private static final String ADD_ACTION = "ADD";
    private static final String TODAY = "today";

    public static Map<String, Object> createLoginPayload(String username, String password) {
        return Map.of(USERNAME.getValue(), username, PASSWORD.getValue(), password);
    }

    public static Map<String, Object> createTraineePayload(String firstName, String lastName) {
        return Map.of(FIRST_NAME.getValue(), firstName,
                LAST_NAME.getValue(), lastName,
                DATE_OF_BIRTH.getValue(), DEFAULT_DATE_OF_BIRTH,
                ADDRESS.getValue(), DEFAULT_ADDRESS);
    }

    public static Map<String, Object> createTraineePayload(Map<String, String> details) {
        return Map.of(FIRST_NAME.getValue(), details.get(FIRST_NAME.getValue()),
                LAST_NAME.getValue(), details.get(LAST_NAME.getValue()),
                DATE_OF_BIRTH.getValue(), details.get(DATE_OF_BIRTH.getValue()),
                ADDRESS.getValue(), details.get(ADDRESS.getValue()));
    }

    public static Map<String, Object> createTrainerPayload(String firstName, String lastName, String specialization) {
        return Map.of(FIRST_NAME.getValue(), firstName, LAST_NAME.getValue(), lastName, SPECIALIZATION.getValue(), specialization);
    }

    public static Map<String, Object> createTrainerPayload(Map<String, String> details) {
        return Map.of(FIRST_NAME.getValue(), details.get(FIRST_NAME.getValue()),
                LAST_NAME.getValue(), details.get(LAST_NAME.getValue()),
                SPECIALIZATION.getValue(), details.get(SPECIALIZATION.getValue()));
    }

    public static Map<String, Object> createTrainingPayload(String traineeUsername, String trainerUsername, int duration) {
        return Map.of(TRAINEE_USERNAME.getValue(), traineeUsername,
                TRAINER_USERNAME.getValue(), trainerUsername,
                TRAINING_NAME.getValue(), DEFAULT_TRAINING_NAME,
                TRAINING_DATE.getValue(), today(),
                TRAINING_DURATION.getValue(), duration);
    }

    public static Map<String, Object> createTrainingPayload(String traineeUsername, String trainerUsername, Map<String, String> details) {
        return Map.of(TRAINEE_USERNAME.getValue(), traineeUsername,
                TRAINER_USERNAME.getValue(), trainerUsername,
                TRAINING_NAME.getValue(), details.get(TRAINING_NAME.getValue()),
                TRAINING_DATE.getValue(), resolveDate(details.get(TRAINING_DATE.getValue())),
                TRAINING_DURATION.getValue(), Integer.parseInt(details.get(TRAINING_DURATION.getValue())));
    }

    public static Map<String, Object> createWorkloadPayload(String trainerUsername, int duration) {
        return Map.of(TRAINER_USERNAME.getValue(), trainerUsername,
                TRAINER_FIRST_NAME.getValue(), DEFAULT_TRAINER_FIRST_NAME,
                TRAINER_LAST_NAME.getValue(), DEFAULT_TRAINER_LAST_NAME,
                IS_ACTIVE.getValue(), true,
                TRAINING_DATE.getValue(), today(),
                TRAINING_DURATION.getValue(), duration,
                ACTION_TYPE.getValue(), ADD_ACTION);
    }

    public static Map<String, Object> createWorkloadPayloadWithoutUsername(int duration) {
        return Map.of(TRAINER_FIRST_NAME.getValue(), DEFAULT_TRAINER_FIRST_NAME,
                TRAINER_LAST_NAME.getValue(), DEFAULT_TRAINER_LAST_NAME,
                IS_ACTIVE.getValue(), true,
                TRAINING_DATE.getValue(), today(),
                TRAINING_DURATION.getValue(), duration,
                ACTION_TYPE.getValue(), ADD_ACTION);
    }

    private static String resolveDate(String date) {
        if (TODAY.equalsIgnoreCase(date)) {
            return today();
        }

        return date;
    }

    private static String today() {
        return LocalDate.now(TEST_ZONE).toString();
    }
}