package com.gym.crm.workload.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT authentication filter tests")
class JwtAuthenticationFilterTest {
    private static final String TOKEN = "valid-token";
    private static final String USERNAME = "billy.herrington";

    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistService blacklistService;
    @Mock
    private MockFilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate request when bearer token is valid")
    void doFilterInternal_whenBearerTokenIsValid_shouldAuthenticateRequest() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, blacklistService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + TOKEN);

        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(blacklistService.isBlacklisted(TOKEN)).thenReturn(false);
        when(jwtService.extractUsername(TOKEN)).thenReturn(USERNAME);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isEqualTo(USERNAME);
        assertThat(authentication.isAuthenticated()).isTrue();
        verify(jwtService).isTokenValid(TOKEN);
        verify(blacklistService).isBlacklisted(TOKEN);
        verify(jwtService).extractUsername(TOKEN);
    }

    @Test
    @DisplayName("Should not authenticate request when token is blacklisted")
    void doFilterInternal_whenTokenIsBlacklisted_shouldNotAuthenticateRequest() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, blacklistService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + TOKEN);

        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(blacklistService.isBlacklisted(TOKEN)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService).isTokenValid(TOKEN);
        verify(blacklistService).isBlacklisted(TOKEN);
        verify(jwtService, never()).extractUsername(TOKEN);
    }

    @Test
    @DisplayName("Should continue filter chain when authorization header is missing")
    void doFilterInternal_whenAuthorizationHeaderIsMissing_shouldNotAuthenticateRequest() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, blacklistService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).isTokenValid(any());
        verify(jwtService, never()).extractUsername(any());
        verify(blacklistService, never()).isBlacklisted(any());
    }

    @Test
    @DisplayName("Should skip authentication when authorization header is not bearer")
    void doFilterInternal_whenAuthorizationHeaderIsNotBearer_shouldSkipAuthentication() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, blacklistService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).isTokenValid(any());
        verify(jwtService, never()).extractUsername(any());
        verify(blacklistService, never()).isBlacklisted(any());
    }

    @Test
    @DisplayName("Should not authenticate when authentication already exists")
    void doFilterInternal_whenAlreadyAuthenticated_shouldNotAuthenticateAgain() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, blacklistService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("existing-user", null, Collections.emptyList()));

        when(jwtService.isTokenValid(TOKEN)).thenReturn(true);
        when(blacklistService.isBlacklisted(TOKEN)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isTokenValid(TOKEN);
        verify(blacklistService).isBlacklisted(TOKEN);
        verify(jwtService, never()).extractUsername(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("existing-user");
    }

    @Test
    @DisplayName("Should not authenticate when token is invalid")
    void doFilterInternal_whenTokenIsInvalid_shouldNotAuthenticate() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, blacklistService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + TOKEN);

        when(jwtService.isTokenValid(TOKEN)).thenReturn(false);
        when(blacklistService.isBlacklisted(TOKEN)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService).isTokenValid(TOKEN);
        verify(blacklistService).isBlacklisted(TOKEN);
        verify(jwtService, never()).extractUsername(any());
    }
}