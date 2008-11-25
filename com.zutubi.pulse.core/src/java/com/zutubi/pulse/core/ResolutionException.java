package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;

/**
 * Exception raised on errors detected by the {@link com.zutubi.pulse.core.ReferenceResolver}.
 */
public class ResolutionException extends PulseException
{
    public ResolutionException(String message)
    {
        super(message);
    }

    public ResolutionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
