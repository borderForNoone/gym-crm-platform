package com.gym.crm.workload.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {
    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void shouldReturn404WhenNoSuchElementExceptionThrown() {
        NoSuchElementException exception = new NoSuchElementException("Trainer workload not found");

        ResponseEntity<Map<String, String>> response = handler.handleNoSuchElement(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnExceptionMessage() {
        String message = "Trainer workload not found: trainer.user";

        ResponseEntity<Map<String, String>> response = handler.handleNoSuchElement(new NoSuchElementException(message));

        assertThat(response.getBody()).containsEntry("message", message);
    }

    @Test
    void shouldContainOnlyMessageField() {
        ResponseEntity<Map<String, String>> response = handler.handleNoSuchElement(new NoSuchElementException("error"));

        assertThat(response.getBody()).hasSize(1).containsKey("message");
    }
}