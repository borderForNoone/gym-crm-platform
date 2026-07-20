package com.gym.crm.workload.exception;

import gym.crm.platform.workload.openapi.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

        ResponseEntity<ErrorResponse> response = handler.handleNoSuchElement(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnExceptionMessage() {
        String message = "Trainer workload not found: trainer.user";

        ResponseEntity<ErrorResponse> response = handler.handleNoSuchElement(new NoSuchElementException(message));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorMessage()).isEqualTo(message);
    }

    @Test
    void shouldReturnErrorCode() {
        ResponseEntity<ErrorResponse> response = handler.handleNoSuchElement(new NoSuchElementException("error"));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnErrorResponseBody() {
        String message = "Trainer workload not found";

        ResponseEntity<ErrorResponse> response = handler.handleNoSuchElement(new NoSuchElementException(message));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorMessage()).isEqualTo(message);
        assertThat(response.getBody().getErrorCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}