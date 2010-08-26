package com.zutubi.pulse.dev.client;

import com.zutubi.pulse.core.api.PulseException;

/**
 * Exception raised by dev clients on error.
 */
public class ClientException extends PulseException
{
    public ClientException()
    {
    }

    public ClientException(String errorMessage)
    {
        super(errorMessage);
    }

    public ClientException(Throwable cause)
    {
        super(cause);
    }

    public ClientException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
