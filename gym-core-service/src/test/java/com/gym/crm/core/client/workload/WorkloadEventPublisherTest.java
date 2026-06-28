package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadEventPublisherTest {
    private static final String USERNAME = "billy.herrington";
    private static final String FIRST_NAME = "Billy";
    private static final String LAST_NAME = "Herrington";
    private static final String DESTINATION = "trainer-workload-queue";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";

    @Mock
    private JmsTemplate jmsTemplate;

    private WorkloadEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new WorkloadEventPublisher(jmsTemplate);
        ReflectionTestUtils.setField(publisher, "destination", DESTINATION);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void publish_shouldSendMessage_toConfiguredDestination() {
        TrainerWorkloadRequest request = buildRequest();

        publisher.publish(request);

        verify(jmsTemplate).convertAndSend(eq(DESTINATION), eq(request), any(MessagePostProcessor.class));
    }

    @Test
    void publish_shouldSetTransactionIdProperty_whenMdcHasValue() throws JMSException {
        String expectedTransactionId = "tx-abc-123";
        MDC.put(MDC_TRANSACTION_ID_KEY, expectedTransactionId);
        TrainerWorkloadRequest request = buildRequest();

        publisher.publish(request);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(DESTINATION), eq(request), captor.capture());

        Message message = mock(Message.class);
        captor.getValue().postProcessMessage(message);

        verify(message).setStringProperty("transactionId", expectedTransactionId);
    }

    @Test
    void publish_shouldNotSetTransactionIdProperty_whenMdcIsEmpty() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();

        publisher.publish(request);

        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(DESTINATION), eq(request), captor.capture());

        Message message = mock(Message.class);
        captor.getValue().postProcessMessage(message);

        verify(message, never()).setStringProperty(anyString(), anyString());
    }

    @Test
    void publish_shouldPropagateException_whenJmsTemplateFails() {
        TrainerWorkloadRequest request = buildRequest();
        doThrow(new InvalidDestinationException(new jakarta.jms.InvalidDestinationException("queue not found")))
                .when(jmsTemplate).convertAndSend(eq(DESTINATION), eq(request), any(MessagePostProcessor.class));

        assertThatThrownBy(() -> publisher.publish(request))
                .isInstanceOf(InvalidDestinationException.class);
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