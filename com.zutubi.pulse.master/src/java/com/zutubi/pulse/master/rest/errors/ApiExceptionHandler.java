package com.zutubi.pulse.master.rest.errors;

import com.zutubi.pulse.master.api.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Decorates all of our controllers with standard exception handling that generates a response body
 * that includes exception details.
 */
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler
{
    @ExceptionHandler
    public final ResponseEntity<Object> handleAny(Exception ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return handleExceptionInternal(ex, null, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return handleExceptionInternal(ex, null, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public final ResponseEntity<Object> handleAuthenticationFailed(Exception ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return handleExceptionInternal(ex, null, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(ValidationException.class)
    public final ResponseEntity<Object> handleValidationFailed(Exception ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        return handleExceptionInternal(ex, null, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request)
    {
        if (body == null)
        {
            // The default implementations all pass in null bodies, so we replace with generic
            // handling here.
            body = new Error(ex);
        }

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * Generic error body, which just exposes details from the underlying exception.
     */
    public static class Error
    {
        private Exception ex;

        public Error(Exception ex)
        {
            this.ex = ex;
        }

        public String getType()
        {
            return ex.getClass().getName();
        }

        public String getMessage()
        {
            return ex.getMessage();
        }
    }
}

