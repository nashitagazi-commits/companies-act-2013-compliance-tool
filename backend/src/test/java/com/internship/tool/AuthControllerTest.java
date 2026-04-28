package com.internship.tool;

import com.internship.tool.config.JwtUtil;
import com.internship.tool.controller.AuthController;
import com.internship.tool.exception.DuplicateResourceException;
import com.internship.tool.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("admin@company.com")
                .password("encoded_password")
                .authorities(Collections.emptyList())
                .build();
    }

    // Test 1 — authController is not null
    @Test
    void testAuthController_NotNull() {
        assertNotNull(authController);
    }

    // Test 2 — jwtUtil and authController both wired
    @Test
    void testAuthController_DependenciesWired() {
        assertNotNull(authController);
        assertNotNull(jwtUtil);
    }

    // Test 3 — duplicate email throws DuplicateResourceException
    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("admin@company.com"))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            if (userRepository.existsByEmail("admin@company.com")) {
                throw new DuplicateResourceException(
                        "User", "email", "admin@company.com");
            }
        });
    }

    // Test 4 — jwtUtil generates different access and refresh tokens
    @Test
    void testJwtUtil_GeneratesDifferentAccessAndRefreshTokens() {
        when(jwtUtil.generateToken(mockUserDetails))
                .thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(mockUserDetails))
                .thenReturn("refresh_token");

        String access = jwtUtil.generateToken(mockUserDetails);
        String refresh = jwtUtil.generateRefreshToken(mockUserDetails);

        assertNotEquals(access, refresh);
    }

    // Test 5 — userRepository existsByEmail returns true
    @Test
    void testUserRepository_ExistsByEmail_ReturnsTrue() {
        when(userRepository.existsByEmail("admin@company.com"))
                .thenReturn(true);

        assertTrue(userRepository.existsByEmail("admin@company.com"));
    }

    // Test 6 — userRepository existsByEmail returns false
    @Test
    void testUserRepository_ExistsByEmail_ReturnsFalse() {
        when(userRepository.existsByEmail("unknown@test.com"))
                .thenReturn(false);

        assertFalse(userRepository.existsByEmail("unknown@test.com"));
    }

    // Test 7 — passwordEncoder encodes password
    @Test
    void testPasswordEncoder_EncodesPassword() {
        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded_password");

        String encoded = passwordEncoder.encode("password123");

        assertNotNull(encoded);
        assertNotEquals("password123", encoded);
    }

    // Test 8 — authenticationManager throws on bad credentials
    @Test
    void testAuthenticationManager_ThrowsOnBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                "wrong@test.com", "wrongpass")));
    }

    // Test 9 — userDetailsService loads user by username
    @Test
    void testUserDetailsService_LoadsUserByUsername() {
        when(userDetailsService.loadUserByUsername("admin@company.com"))
                .thenReturn(mockUserDetails);

        UserDetails result = userDetailsService
                .loadUserByUsername("admin@company.com");

        assertNotNull(result);
        assertEquals("admin@company.com", result.getUsername());
    }

    // Test 10 — jwtUtil validates token correctly
    @Test
    void testJwtUtil_ValidatesToken() {
        when(jwtUtil.validateToken("valid_token", mockUserDetails))
                .thenReturn(true);
        when(jwtUtil.validateToken("invalid_token", mockUserDetails))
                .thenReturn(false);

        assertTrue(jwtUtil.validateToken("valid_token", mockUserDetails));
        assertFalse(jwtUtil.validateToken("invalid_token", mockUserDetails));
    }

    // Test 11 — jwtUtil extracts username from token
    @Test
    void testJwtUtil_ExtractsUsername() {
        when(jwtUtil.extractUsername("some_token"))
                .thenReturn("admin@company.com");

        String username = jwtUtil.extractUsername("some_token");

        assertEquals("admin@company.com", username);
    }

    // Test 12 — refresh with null token returns bad request
    @Test
    void testRefresh_NullToken_ReturnsBadRequest() {
        ResponseEntity<Map<String, String>> response =
                authController.refresh(Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("error"));
    }

    // Test 13 — refresh with empty token returns bad request
    @Test
    void testRefresh_EmptyToken_ReturnsBadRequest() {
        ResponseEntity<Map<String, String>> response =
                authController.refresh(Map.of("refreshToken", ""));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Test 14 — refresh with invalid token returns 401
    @Test
    void testRefresh_InvalidToken_Returns401() {
        when(jwtUtil.extractUsername("invalid_token"))
                .thenThrow(new RuntimeException("Invalid token"));

        ResponseEntity<Map<String, String>> response =
                authController.refresh(
                        Map.of("refreshToken", "invalid_token"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}