package com.gym.crm.workload.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionLoggingFilterTest {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String MDC_TRANSACTION_ID_KEY = "transactionId";

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private TransactionLoggingFilter filter;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/trainer-workloads");
        request.setRemoteAddr("127.0.0.1");

        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldGenerateTransactionId_whenNoneProvided() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        String actualHeader = response.getHeader(TRANSACTION_ID_HEADER);
        assertThat(actualHeader).isNotNull().isNotBlank();
    }

    @Test
    void shouldReuseTransactionId_whenProvidedByUpstream() throws Exception {
        String expected = "upstream-tx-abc-123";
        request.addHeader(TRANSACTION_ID_HEADER, expected);

        filter.doFilterInternal(request, response, filterChain);

        String actual = response.getHeader(TRANSACTION_ID_HEADER);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldHandleBlankTransactionIdHeader_asIfAbsent() throws Exception {
        request.addHeader(TRANSACTION_ID_HEADER, "   ");

        filter.doFilterInternal(request, response, filterChain);

        String actualHeader = response.getHeader(TRANSACTION_ID_HEADER);
        assertThat(actualHeader).isNotNull().isNotBlank().doesNotContain(" ");
    }

    @Test
    void shouldClearMDC_afterRequestCompletes() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        String actual = MDC.get(MDC_TRANSACTION_ID_KEY);
        assertThat(actual).isNull();
    }

    @Test
    void shouldClearMDC_evenWhenFilterChainThrowsException() throws Exception {
        String expectedMessage = "chain failure";
        doThrow(new RuntimeException(expectedMessage)).when(filterChain).doFilter(any(), any());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> filter.doFilterInternal(request, response, filterChain));

        assertThat(thrown.getMessage()).isEqualTo(expectedMessage);
        assertThat(MDC.get(MDC_TRANSACTION_ID_KEY)).isNull();
    }

    @Test
    void shouldPopulateMDC_duringFilterChainExecution() throws Exception {
        String[] capturedTxId = new String[1];
        doAnswer(invocation -> {
            capturedTxId[0] = MDC.get(MDC_TRANSACTION_ID_KEY);return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        String expected = response.getHeader(TRANSACTION_ID_HEADER);
        String actual = capturedTxId[0];
        assertThat(actual).isNotNull().isNotBlank().isEqualTo(expected);
    }

    @Test
    void shouldPassRequestThroughFilterChain() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void shouldReturn200_onSuccessfulRequest() throws Exception {
        int expected = 200;
        response.setStatus(expected);

        filter.doFilterInternal(request, response, filterChain);

        int actual = response.getStatus();
        assertThat(actual).isEqualTo(expected);
    }
}