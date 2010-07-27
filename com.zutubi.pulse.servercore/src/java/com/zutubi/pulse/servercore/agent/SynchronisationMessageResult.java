package com.zutubi.pulse.servercore.agent;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Encapsultes the result of executing a task corresponding to a {@link SynchronisationMessage}.
 */
public class SynchronisationMessageResult
{
    private long messageId;
    private boolean successful;
    private String message;

    /**
     * Creates a successful result.
     * 
     * @param messageId id of the message this result corresponds to
     */
    public SynchronisationMessageResult(long messageId)
    {
        this.messageId = messageId;
        successful = true;
    }

    /**
     * Creates a failed result, with a message extracted from the given
     * exception.
     *
     * @param messageId id of the message this result corresponds to
     * @param exception exception triggered by executing the task
     */
    public SynchronisationMessageResult(long messageId, Exception exception)
    {
        this.messageId = messageId;
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        message = writer.toString();
    }

    /**
     * Creates a result with an explicit status and message.
     *
     * @param messageId  id of the message this result corresponds to
     * @param successful indicates if the task succeeded
     * @param message    a message with more details if the task failed
     */
    public SynchronisationMessageResult(long messageId, boolean successful, String message)
    {
        this.messageId = messageId;
        this.successful = successful;
        this.message = message;
    }

    /**
     * Returns the id of the message that this result corresponds to.
     * 
     * @return the identifier of the corresponding synchronisation message
     */
    public long getMessageId()
    {
        return messageId;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SynchronisationMessageResult that = (SynchronisationMessageResult) o;

        if (messageId != that.messageId)
        {
            return false;
        }
        if (successful != that.successful)
        {
            return false;
        }
        if (message != null ? !message.equals(that.message) : that.message != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (messageId ^ (messageId >>> 32));
        result = 31 * result + (successful ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
