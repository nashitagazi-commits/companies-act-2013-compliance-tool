package com.internship.tool;

import com.internship.tool.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    // Test 1 — handleResourceNotFound returns 404
    @Test
    void testHandleResourceNotFound_Returns404() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Record", "id", 1L);

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
    }

    // Test 2 — handleValidation returns 400
    @Test
    void testHandleValidation_Returns400() {
        Map<String, String> errors = Map.of("title", "Required");
        ValidationException ex = new ValidationException("Failed", errors);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getValidationErrors());
    }

    // Test 3 — handleDuplicateResource returns 409
    @Test
    void testHandleDuplicateResource_Returns409() {
        DuplicateResourceException ex =
                new DuplicateResourceException("User", "email", "test@test.com");

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateResource(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
    }

    // Test 4 — handleBadCredentials returns 401
    @Test
    void testHandleBadCredentials_Returns401() {
        BadCredentialsException ex =
                new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
    }

    // Test 5 — handleAccessDenied returns 403
    @Test
    void testHandleAccessDenied_Returns403() {
        AccessDeniedException ex =
                new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
    }

    // Test 6 — handleAiService returns 503
    @Test
    void testHandleAiService_Returns503() {
        AiServiceException ex = new AiServiceException("AI is down");

        ResponseEntity<ErrorResponse> response =
                handler.handleAiService(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(503, response.getBody().getStatus());
        assertEquals("Service Unavailable", response.getBody().getError());
    }

    // Test 7 — handleGeneral returns 500
    @Test
    void testHandleGeneral_Returns500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    // Test 8 — handleResourceNotFound message contains resource name
    @Test
    void testHandleResourceNotFound_MessageContainsResourceName() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("ComplianceRecord", "id", 99L);

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFound(ex, request);

        assertTrue(response.getBody().getMessage()
                .contains("ComplianceRecord"));
    }

    // Test 9 — handleValidation includes field errors
    @Test
    void testHandleValidation_IncludesFieldErrors() {
        Map<String, String> errors = Map.of(
                "title", "Title required",
                "companyName", "Company required");
        ValidationException ex = new ValidationException("Failed", errors);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(ex, request);

        assertNotNull(response.getBody().getValidationErrors());
        assertEquals(2, response.getBody().getValidationErrors().size());
    }

    // Test 10 — handleMethodArgumentNotValid returns 400
    @Test
    void testHandleMethodArgumentNotValid_Returns400() {
        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "record", "title", "Title is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValid(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals("Title is required",
                response.getBody().getValidationErrors().get("title"));
    }

    // Test 11 — error response has timestamp
    @Test
    void testErrorResponse_HasTimestamp() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Record", "id", 1L);

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFound(ex, request);

        assertNotNull(response.getBody().getTimestamp());
    }

    // Test 12 — handleGeneral message is generic
    @Test
    void testHandleGeneral_MessageIsGeneric() {
        Exception ex = new RuntimeException("Internal details");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(ex, request);

        assertEquals("An unexpected error occurred",
                response.getBody().getMessage());
    }
}