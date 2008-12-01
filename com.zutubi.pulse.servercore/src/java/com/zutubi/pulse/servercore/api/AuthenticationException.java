package com.zutubi.pulse.servercore.api;

import com.zutubi.pulse.core.api.PulseRuntimeException;

/**
 * Thrown when the credentials specified do not provide access to the
 * requested resource.  For example, if a bad or expired token is
 * provided.
 */
public class AuthenticationException extends PulseRuntimeException
{
    public AuthenticationException(String message)
    {
        super(message);
    }
}
