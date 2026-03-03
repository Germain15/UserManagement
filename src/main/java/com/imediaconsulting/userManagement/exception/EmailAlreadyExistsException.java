/**
 * Levée quand un email est déjà présent en base.
 * Interceptée par GlobalExceptionHandler et traduite en HTTP 409 Conflict.
 */
package com.imediaconsulting.userManagement.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}