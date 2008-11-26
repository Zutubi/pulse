package com.zutubi.pulse.core.engine.api;

import com.zutubi.pulse.core.api.PulseRuntimeException;

/**
 * An exception for errors found during build processing that should cause the
 * build to immediately fail.  Note that errors such as compile failures are
 * not considered as exceptional conditions - they are recorded as features and
 * as part of the build result.  An exception should only be raised in response
 * to a severe error that prevents processing of the build or artifacts.
 */
public class BuildException extends PulseRuntimeException
{
    /**
     * Creates a new fatal build exception with the given message.
     *
     * @param errorMessage human-readable message describing the problem
     */
    public BuildException(String errorMessage)
    {
        super(errorMessage);
    }

    /**
     * Creates a new fatal build exception with the given root cause.
     *
     * @param cause exception that is the original cause of failure
     */
    public BuildException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new fatal build exception with the given message and root
     * cause.
     *
     * @param errorMessage human-readable message describing the problem
     * @param cause        exception that is the original cause of failure
     */
    public BuildException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
