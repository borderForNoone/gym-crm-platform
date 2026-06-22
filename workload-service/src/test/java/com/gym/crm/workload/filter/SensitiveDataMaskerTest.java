package com.gym.crm.workload.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataMaskerTest {
    @Test
    void maskAuthHeader_shouldMaskBearerToken() {
        String header = "Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature";
        String expected = "Bearer ***";

        String actual = SensitiveDataMasker.maskAuthHeader(header);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void maskAuthHeader_shouldMaskBasicToken() {
        String header = "Basic dXNlcjpwYXNzd29yZA==";
        String expected = "Basic ***";

        String actual = SensitiveDataMasker.maskAuthHeader(header);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void maskAuthHeader_shouldReturnNull_whenHeaderIsNull() {
        String actual = SensitiveDataMasker.maskAuthHeader(null);

        assertThat(actual).isNull();
    }

    @Test
    void maskAuthHeader_shouldReturnBlank_whenHeaderIsBlank() {
        String expected = "   ";

        String actual = SensitiveDataMasker.maskAuthHeader(expected);

        assertThat(actual).isEqualTo(expected);
    }
}
