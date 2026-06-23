package com.gym.crm.gateway.dto;

import java.time.Instant;

public record FallbackResponse(Instant timestamp, int status, String error, String service, String txId) {
}
