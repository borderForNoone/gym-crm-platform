package com.gym.crm.workload.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);

            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        if (shouldAuthenticate(token)) {
            authenticateRequest(token);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(String token) {
        String username = jwtService.extractUsername(token);
        UsernamePasswordAuthenticationToken authentication = buildAuthentication(username);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean shouldAuthenticate(String token) {
        boolean notAuthenticated = SecurityContextHolder.getContext().getAuthentication() == null;
        boolean valid = jwtService.isTokenValid(token);
        boolean blacklisted = blacklistService.isBlacklisted(token);

        return notAuthenticated && valid && !blacklisted;
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(String username) {
        return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
    }
}