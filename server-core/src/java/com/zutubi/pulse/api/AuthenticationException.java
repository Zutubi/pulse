package com.zutubi.pulse.api;

/**
 * Thrown when the credentials specified do not provide access to the
 * requested resource.  For example, if a bad or expired token is
 * provided.
 */
public class AuthenticationException extends Exception
{
    public AuthenticationException(String message)
    {
        super(message);
    }
}
