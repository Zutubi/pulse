package com.zutubi.validation;

/**
 * Exception used by the validation process to indicate something has prevented
 * the validation from completing successfully.
 */
public class ValidationException extends Exception
{
    public ValidationException()
    {
    }

    public ValidationException(String message)
    {
        super(message);
    }

    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ValidationException(Throwable cause)
    {
        super(cause);
    }
}
