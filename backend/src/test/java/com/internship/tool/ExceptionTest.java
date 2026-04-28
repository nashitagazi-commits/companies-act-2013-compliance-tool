package com.internship.tool;

import com.internship.tool.exception.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testResourceNotFoundException_Message() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Record", "id", 1L);
        assertTrue(ex.getMessage().contains("Record"));
        assertTrue(ex.getMessage().contains("id"));
        assertEquals("Record", ex.getResourceName());
        assertEquals("id", ex.getFieldName());
        assertEquals(1L, ex.getFieldValue());
    }

    @Test
    void testValidationException_WithMessage() {
        ValidationException ex = new ValidationException("Validation failed");
        assertEquals("Validation failed", ex.getMessage());
        assertNotNull(ex.getErrors());
        assertTrue(ex.getErrors().isEmpty());
    }

    @Test
    void testValidationException_WithErrors() {
        Map<String, String> errors = Map.of("title", "Title is required");
        ValidationException ex = new ValidationException("Failed", errors);
        assertEquals("Failed", ex.getMessage());
        assertEquals("Title is required", ex.getErrors().get("title"));
    }

    @Test
    void testDuplicateResourceException_Message() {
        DuplicateResourceException ex =
                new DuplicateResourceException("User", "email", "test@test.com");
        assertTrue(ex.getMessage().contains("User"));
        assertTrue(ex.getMessage().contains("email"));
        assertEquals("User", ex.getResourceName());
        assertEquals("email", ex.getFieldName());
        assertEquals("test@test.com", ex.getFieldValue());
    }

    @Test
    void testAiServiceException_Message() {
        AiServiceException ex = new AiServiceException("AI down");
        assertEquals("AI down", ex.getMessage());
        assertTrue(ex.isFallback());
    }

    @Test
    void testAiServiceException_WithCause() {
        RuntimeException cause = new RuntimeException("timeout");
        AiServiceException ex = new AiServiceException("AI failed", cause);
        assertEquals("AI failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
        assertTrue(ex.isFallback());
    }

    @Test
    void testErrorResponse_FactoryMethod() {
        ErrorResponse response = ErrorResponse.of(
                404, "Not Found", "Record not found", "/api/test");
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Record not found", response.getMessage());
        assertEquals("/api/test", response.getPath());
        assertNotNull(response.getTimestamp());
        assertNull(response.getValidationErrors());
    }

    @Test
    void testErrorResponse_WithValidation() {
        Map<String, String> errors = Map.of("title", "Required");
        ErrorResponse response = ErrorResponse.withValidation(
                400, "Bad Request", "Validation failed", "/api/test", errors);
        assertEquals(400, response.getStatus());
        assertNotNull(response.getValidationErrors());
        assertEquals("Required", response.getValidationErrors().get("title"));
    }

    @Test
    void testErrorResponse_Builder() {
        ErrorResponse response = ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("Unexpected error")
                .build();
        assertEquals(500, response.getStatus());
        assertEquals("Internal Server Error", response.getError());
    }
}