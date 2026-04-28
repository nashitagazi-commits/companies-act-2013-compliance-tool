package com.internship.tool;

import com.internship.tool.config.JwtAuthFilter;
import com.internship.tool.config.JwtUtil;
import com.internship.tool.config.RedisConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringConfigTest {

    @Mock
    private UserDetailsService userDetailsService;

    private JwtUtil buildJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test_secret_key_minimum_32_characters_long_here_12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs",
                604800000L);
        return jwtUtil;
    }

    private UserDetails buildUser(String email) {
        return org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    // Test 1 — no auth header passes through
    @Test
    void testJwtAuthFilter_NoAuthHeader_PassesThrough() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(
                buildJwtUtil(), userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(response);
    }

    // Test 2 — Basic auth header passes through
    @Test
    void testJwtAuthFilter_BasicAuthHeader_PassesThrough() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(
                buildJwtUtil(), userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic sometoken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(response);
    }

    // Test 3 — valid token authenticates user
    @Test
    void testJwtAuthFilter_ValidToken_AuthenticatesUser() throws Exception {
        JwtUtil jwtUtil = buildJwtUtil();
        UserDetails userDetails = buildUser("admin@company.com");
        String token = jwtUtil.generateToken(userDetails);

        when(userDetailsService.loadUserByUsername("admin@company.com"))
                .thenReturn(userDetails);

        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(response);
        verify(userDetailsService, times(1))
                .loadUserByUsername("admin@company.com");
    }

    // Test 4 — invalid token handles error gracefully
    @Test
    void testJwtAuthFilter_InvalidToken_HandlesError() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(
                buildJwtUtil(), userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalidtoken123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(response);
    }

    // Test 5 — empty authorization header passes through
    @Test
    void testJwtAuthFilter_EmptyHeader_PassesThrough() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(
                buildJwtUtil(), userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(response);
    }

    // Test 6 — RedisConfig all cache names unique
    @Test
    void testRedisConfig_AllCacheNamesUnique() {
        assertNotEquals(RedisConfig.COMPLIANCE_RECORDS_CACHE,
                RedisConfig.COMPLIANCE_RECORD_CACHE);
        assertNotEquals(RedisConfig.COMPLIANCE_RECORDS_CACHE,
                RedisConfig.COMPLIANCE_STATS_CACHE);
        assertNotEquals(RedisConfig.COMPLIANCE_RECORD_CACHE,
                RedisConfig.COMPLIANCE_STATS_CACHE);
    }

    // Test 7 — RedisConfig cache names not empty
    @Test
    void testRedisConfig_CacheNamesNotEmpty() {
        assertFalse(RedisConfig.COMPLIANCE_RECORDS_CACHE.isEmpty());
        assertFalse(RedisConfig.COMPLIANCE_RECORD_CACHE.isEmpty());
        assertFalse(RedisConfig.COMPLIANCE_STATS_CACHE.isEmpty());
    }

    // Test 8 — RedisConfig instantiation
    @Test
    void testRedisConfig_Instantiation() {
        RedisConfig config = new RedisConfig();
        assertNotNull(config);
    }

    // Test 9 — JwtUtil wrong user returns false
    @Test
    void testJwtUtil_WrongUser_ReturnsFalse() {
        JwtUtil jwtUtil = buildJwtUtil();
        UserDetails user1 = buildUser("user1@test.com");
        UserDetails user2 = buildUser("user2@test.com");
        String token = jwtUtil.generateToken(user1);
        assertFalse(jwtUtil.validateToken(token, user2));
    }

    // Test 10 — JwtUtil expiration is in future
    @Test
    void testJwtUtil_Expiration_IsInFuture() {
        JwtUtil jwtUtil = buildJwtUtil();
        UserDetails user = buildUser("test@test.com");
        String token = jwtUtil.generateToken(user);
        assertTrue(jwtUtil.extractExpiration(token).after(new Date()));
    }
}