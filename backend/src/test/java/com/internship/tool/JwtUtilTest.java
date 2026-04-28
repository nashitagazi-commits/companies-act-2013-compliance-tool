package com.internship.tool;

import com.internship.tool.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test_secret_key_minimum_32_characters_long_here_12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs", 604800000L);

        userDetails = User.withUsername("admin@company.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void testGenerateToken_NotNull() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername_ReturnsCorrectEmail() {
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        assertEquals("admin@company.com", username);
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testValidateToken_WrongUser_ReturnsFalse() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails otherUser = User.withUsername("other@company.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        assertFalse(jwtUtil.validateToken(token, otherUser));
    }

    @Test
    void testGenerateRefreshToken_NotNull() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void testExtractUsername_FromRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        String username = jwtUtil.extractUsername(refreshToken);
        assertEquals("admin@company.com", username);
    }

    @Test
    void testExtractExpiration_NotNull() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(jwtUtil.extractExpiration(token));
    }

    @Test
    void testGenerateToken_DifferentUsers_DifferentTokens() {
        UserDetails user2 = User.withUsername("manager@company.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        String token1 = jwtUtil.generateToken(userDetails);
        String token2 = jwtUtil.generateToken(user2);
        assertNotEquals(token1, token2);
    }
}