package com.gym.crm.workload.messaging;

import gym.crm.platform.workload.openapi.ActionType;
import gym.crm.platform.workload.openapi.TrainerWorkloadRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeadLetterPublisherTest {
    private static final String USERNAME = "billy.herrington";
    private static final String DLQ_DESTINATION = "trainer-workload-dlq";
    private static final String SOURCE_DESTINATION = "trainer-workload-queue";
    private static final String REASON = "trainerUsername is required";
    private static final String TRANSACTION_ID = "tx-123";

    @Mock
    private JmsTemplate jmsTemplate;

    private DeadLetterPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new DeadLetterPublisher(jmsTemplate);
        ReflectionTestUtils.setField(publisher, "dlqDestination", DLQ_DESTINATION);
        ReflectionTestUtils.setField(publisher, "sourceDestination", SOURCE_DESTINATION);
    }

    @Test
    void send_shouldForwardMessage_toDlqDestination() {
        TrainerWorkloadRequest request = buildRequest();

        publisher.send(request, REASON, TRANSACTION_ID);

        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(request), any(MessagePostProcessor.class));
    }

    @Test
    void send_shouldSetFailureReasonAndOriginalDestinationProperties() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();
        Message message = mock(Message.class);
        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        publisher.send(request, REASON, TRANSACTION_ID);

        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(request), captor.capture());
        captor.getValue().postProcessMessage(message);
        verify(message).setStringProperty("failureReason", REASON);
        verify(message).setStringProperty("originalDestination", SOURCE_DESTINATION);
        verify(message).setStringProperty("transactionId", TRANSACTION_ID);
    }

    @Test
    void send_shouldNotSetTransactionIdProperty_whenTransactionIdIsNull() throws JMSException {
        TrainerWorkloadRequest request = buildRequest();
        ArgumentCaptor<MessagePostProcessor> captor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        Message message = mock(Message.class);

        publisher.send(request, REASON, null);

        verify(jmsTemplate).convertAndSend(eq(DLQ_DESTINATION), eq(request), captor.capture());
        captor.getValue().postProcessMessage(message);
        verify(message, never()).setStringProperty(eq("transactionId"), anyString());
    }

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .isActive(true)
                .actionType(ActionType.ADD);
    }
}