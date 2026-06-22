package com.gym.crm.workload.filter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensitiveDataMasker {
    private static final String MASK = "***";
    private static final Pattern AUTH_HEADER_PATTERN = Pattern.compile("(Bearer|Basic)\\s+[A-Za-z0-9+/=._-]+", Pattern.CASE_INSENSITIVE);

    public static String maskAuthHeader(String header) {
        if (header == null || header.isBlank()) {
            return header;
        }

        return AUTH_HEADER_PATTERN.matcher(header).replaceAll("$1 " + MASK);
    }
}
