package com.internship.tool;

import com.internship.tool.config.JwtUtil;
import com.internship.tool.config.RedisConfig;
import com.internship.tool.config.SwaggerConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    // --- SwaggerConfig tests ---

    @Test
    void testSwaggerConfig_OpenAPIBean_NotNull() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI openAPI = config.openAPI();
        assertNotNull(openAPI);
    }

    @Test
    void testSwaggerConfig_OpenAPI_HasTitle() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI openAPI = config.openAPI();
        assertNotNull(openAPI.getInfo());
        assertEquals("Companies Act 2013 Compliance Tool",
                openAPI.getInfo().getTitle());
    }

    @Test
    void testSwaggerConfig_OpenAPI_HasVersion() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI openAPI = config.openAPI();
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void testSwaggerConfig_OpenAPI_HasSecurityRequirement() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI openAPI = config.openAPI();
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
    }

    @Test
    void testSwaggerConfig_OpenAPI_HasComponents() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI openAPI = config.openAPI();
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes()
                .containsKey("bearerAuth"));
    }

    // --- RedisConfig constants tests ---

    @Test
    void testRedisConfig_CacheNames_NotNull() {
        assertNotNull(RedisConfig.COMPLIANCE_RECORDS_CACHE);
        assertNotNull(RedisConfig.COMPLIANCE_RECORD_CACHE);
        assertNotNull(RedisConfig.COMPLIANCE_STATS_CACHE);
    }

    @Test
    void testRedisConfig_CacheNames_CorrectValues() {
        assertEquals("complianceRecords",
                RedisConfig.COMPLIANCE_RECORDS_CACHE);
        assertEquals("complianceRecord",
                RedisConfig.COMPLIANCE_RECORD_CACHE);
        assertEquals("complianceStats",
                RedisConfig.COMPLIANCE_STATS_CACHE);
    }

    // --- JwtUtil additional tests ---

    @Test
    void testJwtUtil_TokenHasThreeParts() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test_secret_key_minimum_32_characters_long_here_12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs",
                604800000L);

        org.springframework.security.core.userdetails.UserDetails user =
                org.springframework.security.core.userdetails.User
                        .withUsername("test@test.com")
                        .password("pass")
                        .authorities(java.util.Collections.emptyList())
                        .build();

        String token = jwtUtil.generateToken(user);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void testJwtUtil_RefreshTokenHasThreeParts() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test_secret_key_minimum_32_characters_long_here_12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs",
                604800000L);

        org.springframework.security.core.userdetails.UserDetails user =
                org.springframework.security.core.userdetails.User
                        .withUsername("test@test.com")
                        .password("pass")
                        .authorities(java.util.Collections.emptyList())
                        .build();

        String token = jwtUtil.generateRefreshToken(user);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void testJwtUtil_ExtractUsername_AfterGenerate() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test_secret_key_minimum_32_characters_long_here_12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs",
                604800000L);

        org.springframework.security.core.userdetails.UserDetails user =
                org.springframework.security.core.userdetails.User
                        .withUsername("manager@test.com")
                        .password("pass")
                        .authorities(java.util.Collections.emptyList())
                        .build();

        String token = jwtUtil.generateToken(user);
        assertEquals("manager@test.com", jwtUtil.extractUsername(token));
    }

    @Test
    void testJwtUtil_ValidateToken_ExpiredToken_ReturnsFalse() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test_secret_key_minimum_32_characters_long_here_12345");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 1L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs", 1L);

        org.springframework.security.core.userdetails.UserDetails user =
                org.springframework.security.core.userdetails.User
                        .withUsername("test@test.com")
                        .password("pass")
                        .authorities(java.util.Collections.emptyList())
                        .build();

        String token = jwtUtil.generateToken(user);

        try { Thread.sleep(10); } catch (InterruptedException e) { }

        assertThrows(Exception.class,
                () -> jwtUtil.validateToken(token, user));
    }
}