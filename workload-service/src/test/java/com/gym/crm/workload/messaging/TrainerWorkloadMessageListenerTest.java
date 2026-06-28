package com.gym.crm.workload.messaging;

import com.gym.crm.workload.service.TrainerWorkloadService;
import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {
    private static final String TRANSACTION_ID_PROPERTY = "transactionId";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";
    private static final String USERNAME = "billy.herrington";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";

    @Mock
    private TrainerWorkloadService trainerWorkloadService;
    @Mock
    private Message message;

    private TrainerWorkloadMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new TrainerWorkloadMessageListener(trainerWorkloadService);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void onMessage_shouldCallService_whenRequestIsValid() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");
        TrainerWorkloadRequest request = buildRequest();

        listener.onMessage(request, message);

        verify(trainerWorkloadService).updateTrainerWorkload(request);
    }

    @Test
    void onMessage_shouldClearMdc_afterProcessing() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");
        TrainerWorkloadRequest request = buildRequest();

        listener.onMessage(request, message);

        String actual = MDC.get(MDC_TRANSACTION_ID_KEY);
        assertThat(actual).isNull();
    }

    @Test
    void onMessage_shouldClearMdc_evenWhenValidationFails() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");
        TrainerWorkloadRequest invalidRequest = buildRequest().trainerUsername(null);
        String actualTransactionId = MDC.get(MDC_TRANSACTION_ID_KEY);

        Throwable actual = catchThrowable(() -> listener.onMessage(invalidRequest, message));

        assertThat(actual).isInstanceOf(IllegalArgumentException.class);
        assertThat(actualTransactionId).isNull();
    }

    @Test
    void onMessage_shouldUseNullTransactionId_whenHeaderExtractionThrows() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenThrow(new JMSException("broker error"));
        TrainerWorkloadRequest request = buildRequest();

        listener.onMessage(request, message);

        verify(trainerWorkloadService).updateTrainerWorkload(request);
    }

    @Test
    void onMessage_shouldThrowIllegalArgumentException_whenTrainerUsernameIsNull() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");
        TrainerWorkloadRequest request = buildRequest().trainerUsername(null);

        Throwable actual = catchThrowable(() -> listener.onMessage(request, message));

        assertThat(actual).isInstanceOf(IllegalArgumentException.class).hasMessage("trainerUsername is required");
        verify(trainerWorkloadService, never()).updateTrainerWorkload(request);
    }

    @Test
    void onMessage_shouldThrowIllegalArgumentException_whenTrainerUsernameIsBlank() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");
        TrainerWorkloadRequest request = buildRequest().trainerUsername("   ");

        Throwable actual = catchThrowable(() -> listener.onMessage(request, message));

        assertThat(actual).isInstanceOf(IllegalArgumentException.class).hasMessage("trainerUsername is required");
    }

    @Test
    void onMessage_shouldThrowIllegalArgumentException_whenTrainingDateIsNull() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");
        TrainerWorkloadRequest request = buildRequest().trainingDate(null);

        Throwable actual = catchThrowable(() -> listener.onMessage(request, message));

        assertThat(actual).isInstanceOf(IllegalArgumentException.class).hasMessage("trainingDate is required");
        verify(trainerWorkloadService, never()).updateTrainerWorkload(request);
    }

    @Test
    void onMessage_shouldThrowIllegalArgumentException_whenActionTypeIsNull() throws JMSException {
        when(message.getStringProperty(TRANSACTION_ID_PROPERTY)).thenReturn("tx-123");
        TrainerWorkloadRequest request = buildRequest().actionType(null);

        Throwable actual = catchThrowable(() -> listener.onMessage(request, message));

        assertThat(actual).isInstanceOf(IllegalArgumentException.class).hasMessage("actionType is required");
        verify(trainerWorkloadService, never()).updateTrainerWorkload(request);
    }

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(2026, Month.JUNE, 10))
                .trainingDuration(60)
                .actionType(ActionType.ADD);
    }
}