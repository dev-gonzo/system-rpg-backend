package br.com.systemrpg.backend.exception.handler;

import br.com.systemrpg.backend.constants.MessageConstants;
import br.com.systemrpg.backend.exception.AlreadyExistsException;
import br.com.systemrpg.backend.exception.BusinessException;
import br.com.systemrpg.backend.exception.RecordNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.Collections;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RestExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @Mock
    private MessageSource messageSource;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private RestExceptionHandler restExceptionHandler;

    @Test
    void handleNotFound_ShouldReturnNotFoundResponse() {
        // Arrange
        RecordNotFoundException exception = new RecordNotFoundException("Record not found");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users/999");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleNotFound(webRequest, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Record not found", response.getBody().getMessage());
        assertEquals("/api/users/999", response.getBody().getPath());
        assertTrue(response.getBody().getFieldErrors().isEmpty());
        assertEquals(response.getBody().getStatus(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    void handleAlreadyExists_ShouldReturnBadRequestResponse() {
        // Arrange
        AlreadyExistsException exception = new AlreadyExistsException("User already exists");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleAlreadyExists(webRequest, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User already exists", response.getBody().getMessage());
        assertEquals("/api/users", response.getBody().getPath());
        assertTrue(response.getBody().getFieldErrors().isEmpty());
        assertEquals(response.getBody().getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void handleIllegalArgument_ShouldReturnBadRequestResponse() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/validate");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleIllegalArgument(webRequest, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument provided", response.getBody().getMessage());
        assertEquals("/api/validate", response.getBody().getPath());
        assertTrue(response.getBody().getFieldErrors().isEmpty());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void handleIllegalState_ShouldReturnInternalServerErrorResponse() {
        // Arrange
        IllegalStateException exception = new IllegalStateException("Invalid state");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/process");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleIllegalState(webRequest, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid state", response.getBody().getMessage());
        assertEquals("/api/process", response.getBody().getPath());
        assertTrue(response.getBody().getFieldErrors().isEmpty());
        assertEquals(response.getBody().getStatus(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void handleBusiness_WithFieldErrors_ShouldReturnResponseWithFieldErrors() {
        // Arrange
        BusinessException exception = new BusinessException("Business validation failed", HttpStatus.BAD_REQUEST);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/business");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleBusiness(webRequest, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Business validation failed", response.getBody().getMessage());
        assertEquals("/api/business", response.getBody().getPath());
        assertNotNull(response.getBody().getFieldErrors());
    }

    @Test
    void handleBusiness_WithoutFieldErrors_ShouldReturnResponseWithEmptyFieldErrors() {
        // Arrange
        BusinessException exception = new BusinessException("Business error", HttpStatus.CONFLICT);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/business");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleBusiness(webRequest, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Business error", response.getBody().getMessage());
        assertEquals("/api/business", response.getBody().getPath());
        assertNotNull(response.getBody().getFieldErrors());
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnBadRequestWithFieldErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult mockBindingResult = mock(BindingResult.class);
        HttpHeaders headers = new HttpHeaders();
        
        FieldError fieldError1 = new FieldError("user", "name", "Name cannot be blank");
        FieldError fieldError2 = new FieldError("user", "email", "Email must be valid");
        
        when(exception.getBindingResult()).thenReturn(mockBindingResult);
        when(mockBindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");
        when(messageSource.getMessage(eq(MessageConstants.INVALID_FIELDS), any(), any())).thenReturn("Invalid fields provided");

        // Act
        ResponseEntity<Object> response = restExceptionHandler.handleMethodArgumentNotValid(
            exception, headers, HttpStatus.BAD_REQUEST, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        RestResponse restResponse = (RestResponse) response.getBody();
        assertNotNull(restResponse.getMessage());
        assertEquals("/api/users", restResponse.getPath());
        assertEquals(2, restResponse.getFieldErrors().size());
        assertEquals("name", restResponse.getFieldErrors().get(0).getField());
        assertEquals("Name cannot be blank", restResponse.getFieldErrors().get(0).getError());
        assertEquals("email", restResponse.getFieldErrors().get(1).getField());
        assertEquals("Email must be valid", restResponse.getFieldErrors().get(1).getError());
    }

    @Test
    void handleMethodArgumentNotValid_WithNoFieldErrors_ShouldReturnBadRequestWithEmptyFieldErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        HttpHeaders headers = new HttpHeaders();
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");

        // Act
        ResponseEntity<Object> response = restExceptionHandler.handleMethodArgumentNotValid(
            exception, headers, HttpStatus.BAD_REQUEST, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        RestResponse restResponse = (RestResponse) response.getBody();
        assertTrue(restResponse.getFieldErrors().isEmpty());
    }

    @Test
    void getMessage_WithMessageSourceKey_ShouldReturnMessage() {
        // Arrange
        String messageKey = "br.com.systemrpg.error.validation";
        String expectedMessage = "Validation error occurred";
        RecordNotFoundException exception = new RecordNotFoundException(messageKey);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
        when(messageSource.getMessage(eq(messageKey), any(), any())).thenReturn(expectedMessage);

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleNotFound(webRequest, exception);

        // Assert
        assertNotNull(response.getBody().getMessage());
        assertEquals(expectedMessage, response.getBody().getMessage());
    }

    @Test
    void getMessage_WithNonMessageSourceKey_ShouldReturnOriginalMessage() {
        // Arrange
        String message = "Simple error message";
        RecordNotFoundException exception = new RecordNotFoundException(message);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleNotFound(webRequest, exception);

        // Assert
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void getMessage_WithNullMessageSource_ShouldReturnOriginalMessage() {
        // Arrange
        String messageKey = "br.com.systemrpg.error.test";
        RecordNotFoundException exception = new RecordNotFoundException(messageKey);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
        when(messageSource.getMessage(eq(messageKey), any(), any())).thenThrow(new RuntimeException("Message not found"));

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleNotFound(webRequest, exception);

        // Assert
        assertNotNull(response.getBody().getMessage());
        assertEquals(messageKey, response.getBody().getMessage());
    }

    @Test
    void getMessage_WithMessageSourceNull_ShouldReturnOriginalMessage() {
        // Arrange
        String messageKey = "br.com.systemrpg.error.test";
        RestExceptionHandler handlerWithNullMessageSource = new RestExceptionHandler(null);
        RecordNotFoundException exception = new RecordNotFoundException(messageKey);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");

        // Act
        ResponseEntity<RestResponse> response = handlerWithNullMessageSource.handleNotFound(webRequest, exception);

        // Assert
        assertNotNull(response.getBody().getMessage());
        assertEquals(messageKey, response.getBody().getMessage());
    }

    @Test
    void restResponse_SetDetailAndHelp_ShouldSetValues() {
        // Arrange
        RestResponse response = new RestResponse();
        String detail = "Detailed error information";
        String help = "Help information for resolving the error";

        // Act
        response.setDetail(detail);
        response.setHelp(help);

        // Assert
        assertEquals(detail, response.getDetail());
        assertEquals(help, response.getHelp());
    }

    @Test
    void getPath_ShouldExtractPathFromWebRequest() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test/path");
        RecordNotFoundException exception = new RecordNotFoundException("Test");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleNotFound(webRequest, exception);

        // Assert
        assertEquals("/api/test/path", response.getBody().getPath());
    }

    @Test
    void getPath_WithShortDescription_ShouldHandleGracefully() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=");
        RecordNotFoundException exception = new RecordNotFoundException("Test");

        // Act
        ResponseEntity<RestResponse> response = restExceptionHandler.handleNotFound(webRequest, exception);

        // Assert
        assertEquals("", response.getBody().getPath());
    }

    @Test
    void allHandlers_ShouldReturnRestResponseWithCorrectStructure() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
        
        RecordNotFoundException notFoundException = new RecordNotFoundException("Not found");
        AlreadyExistsException existsException = new AlreadyExistsException("Already exists");
        IllegalArgumentException illegalArgException = new IllegalArgumentException("Illegal argument");
        IllegalStateException illegalStateException = new IllegalStateException("Illegal state");
        BusinessException businessException = new BusinessException("Business error", HttpStatus.BAD_REQUEST);

        // Act & Assert
        ResponseEntity<RestResponse> notFoundResponse = restExceptionHandler.handleNotFound(webRequest, notFoundException);
        ResponseEntity<RestResponse> existsResponse = restExceptionHandler.handleAlreadyExists(webRequest, existsException);
        ResponseEntity<RestResponse> illegalArgResponse = restExceptionHandler.handleIllegalArgument(webRequest, illegalArgException);
        ResponseEntity<RestResponse> illegalStateResponse = restExceptionHandler.handleIllegalState(webRequest, illegalStateException);
        ResponseEntity<RestResponse> businessResponse = restExceptionHandler.handleBusiness(webRequest, businessException);

        // Verify all responses have the expected structure
        verifyRestResponseStructure(notFoundResponse.getBody());
        verifyRestResponseStructure(existsResponse.getBody());
        verifyRestResponseStructure(illegalArgResponse.getBody());
        verifyRestResponseStructure(illegalStateResponse.getBody());
        verifyRestResponseStructure(businessResponse.getBody());
    }

    private void verifyRestResponseStructure(RestResponse response) {
        assertNotNull(response.getTimestamp());
        assertTrue(response.getStatus() > 0);
        assertNotNull(response.getError());
        assertNotNull(response.getMessage());
        assertNotNull(response.getPath());
        assertNotNull(response.getFieldErrors());
    }
}
