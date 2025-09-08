package br.com.systemrpg.backend.util;

import br.com.systemrpg.backend.dto.response.ApiResponse;
import br.com.systemrpg.backend.dto.response.ResponseApi;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ResponseUtilTest {

    @Test
    void ok_WithData_ShouldReturnOkResponse() {
        String data = "test data";
        ResponseEntity<String> response = ResponseUtil.ok(data);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(data, response.getBody());
    }

    @Test
    void success_WithDataAndMessage_ShouldReturnSuccessApiResponse() {
        String data = "test data";
        String message = "Success message";
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.success(data, message);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(data, response.getBody().getData());
    }

    @Test
    void success_WithMessageOnly_ShouldReturnSuccessApiResponse() {
        String message = "Success message";
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.success(message);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void created_WithData_ShouldReturnCreatedResponse() {
        String data = "created data";
        ResponseEntity<String> response = ResponseUtil.created(data);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(data, response.getBody());
    }

    @Test
    void created_WithDataAndMessage_ShouldReturnCreatedApiResponse() {
        String data = "created data";
        String message = "Created successfully";
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.created(data, message);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(data, response.getBody().getData());
    }

    @Test
    void badRequest_WithMessage_ShouldReturnBadRequestApiResponse() {
        String message = "Bad request message";
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.badRequest(message);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void badRequest_WithMessageAndDetails_ShouldReturnBadRequestErrorResponse() {
        String message = "Bad request";
        String details = "Invalid input";
        ResponseEntity<ResponseApi<String>> response = ResponseUtil.badRequest(message, details);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(details, response.getBody().getData());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void unauthorized_WithMessage_ShouldReturnUnauthorizedApiResponse() {
        String message = "Unauthorized access";
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.unauthorized(message);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void unauthorized_WithMessageAndDetails_ShouldReturnUnauthorizedErrorResponse() {
        String message = "Unauthorized";
        String details = "Invalid token";
        ResponseEntity<ResponseApi<String>> response = ResponseUtil.unauthorized(message, details);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(details, response.getBody().getData());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void forbidden_WithMessage_ShouldReturnForbiddenApiResponse() {
        String message = "Access forbidden";
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.forbidden(message);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void notFound_WithMessage_ShouldReturnNotFoundApiResponse() {
        String message = "Resource not found";
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.notFound(message);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void conflict_WithMessage_ShouldReturnConflictApiResponse() {
        String message = "Resource conflict";
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.conflict(message);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void internalServerError_WithMessage_ShouldReturnInternalServerErrorApiResponse() {
        String message = "Internal server error";
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.internalServerError(message);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void internalServerError_WithMessageAndDetails_ShouldReturnInternalServerErrorErrorResponse() {
        String message = "Server error";
        String details = "Database connection failed";
        ResponseEntity<ResponseApi<String>> response = ResponseUtil.internalServerError(message, details);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(details, response.getBody().getData());
        assertEquals(message, response.getBody().getError());
    }

    @Test
    void status_WithCustomStatusAndData_ShouldReturnCustomStatusResponse() {
        String data = "custom data";
        ResponseEntity<String> response = ResponseUtil.status(HttpStatus.ACCEPTED, data);
        
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(data, response.getBody());
    }

    @Test
    void status_WithSuccessStatusDataAndMessage_ShouldReturnSuccessApiResponse() {
        String data = "success data";
        String message = "Operation successful";
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.status(HttpStatus.OK, data, message);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(data, response.getBody().getData());
    }

    @Test
    void status_WithErrorStatusDataAndMessage_ShouldReturnErrorApiResponse() {
        String data = "error data";
        String message = "Operation failed";
        ResponseEntity<ApiResponse<String>> response = ResponseUtil.status(HttpStatus.BAD_REQUEST, data, message);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getError());
        assertEquals(data, response.getBody().getData());
    }

    @Test
    void noContent_ShouldReturnNoContentResponse() {
        ResponseEntity<Void> response = ResponseUtil.noContent();
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void accepted_WithMessage_ShouldReturnAcceptedApiResponse() {
        String message = "Request accepted";
        ResponseEntity<ApiResponse<Void>> response = ResponseUtil.accepted(message);
        
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void successResponse_WithDataAndMessage_ShouldReturnSuccessResponse() {
        String data = "test data";
        String message = "Success";
        ResponseApi<String> response = ResponseUtil.successResponse(data, message);
        
        assertNotNull(response);
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void successResponse_WithMessageOnly_ShouldReturnSuccessResponse() {
        String message = "Success";
        ResponseApi<Void> response = ResponseUtil.successResponse(message);
        
        assertNotNull(response);
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void okWithSuccess_WithDataAndMessage_ShouldReturnOkSuccessResponse() {
        String data = "test data";
        String message = "Success";
        ResponseEntity<ResponseApi<String>> response = ResponseUtil.okWithSuccess(data, message);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(data, response.getBody().getData());
    }

    @Test
    void createdWithSuccess_WithDataAndMessage_ShouldReturnCreatedSuccessResponse() {
        String data = "created data";
        String message = "Created successfully";
        ResponseEntity<ResponseApi<String>> response = ResponseUtil.createdWithSuccess(data, message);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(data, response.getBody().getData());
    }
}
