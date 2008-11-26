package com.zutubi.pulse.core.engine.api;

import com.zutubi.pulse.core.api.PulseException;

/**
 * Exception to raise when an error occurs setting the property of a Pulse file
 * element.  This causes Pulse file loading to fail with an error message.
 */
public class FileLoadException extends PulseException
{
    /**
     * Creates a file load exception with no details.
     */
    public FileLoadException()
    {
        super();
    }

    /**
     * Creates a file load exception with the given message.
     *
     * @param errorMessage human-readble description of the failure
     */
    public FileLoadException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Creates a file load exception with the given root cause.
     *
     * @param cause exception that is the root cause of the failure
     */
    public FileLoadException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a file load exception with the given message and root cause.
     *
     * @param errorMessage human-readble description of the failure
     * @param cause        exception that is the root cause of the failure
     */
    public FileLoadException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
