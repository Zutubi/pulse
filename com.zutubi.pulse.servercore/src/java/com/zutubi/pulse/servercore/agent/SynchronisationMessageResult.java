package com.zutubi.pulse.servercore.agent;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Encapsultes the result of executing a task corresponding to a {@link SynchronisationMessage}.
 */
public class SynchronisationMessageResult
{
    private boolean successful;
    private String message;

    /**
     * Creates a successful result.
     */
    public SynchronisationMessageResult()
    {
        successful = true;
    }

    /**
     * Creates a failed result, with a message extracted from the given
     * exception.
     *
     * @param exception exception triggered by executing the task
     */
    public SynchronisationMessageResult(Exception exception)
    {
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        message = writer.toString();
    }

    /**
     * Creates a result with an explicit status and message.
     *
     * @param successful indicates if the task succeeded
     * @param message    a message with more details if the task failed
     */
    public SynchronisationMessageResult(boolean successful, String message)
    {
        this.successful = successful;
        this.message = message;
    }

    /**
     * Indicates if the task corresponding to the message succeeded.
     *
     * @return true if the task succeeded, false if it failed
     */
    public boolean isSuccessful()
    {
        return successful;
    }

    /**
     * If the task failed, gives further details.
     *
     * @return a human-readable message describing how and/or why the task
     *         failed
     */
    public String getMessage()
    {
        return message;
    }
}
