package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Wraps a {@link com.zutubi.pulse.servercore.agent.SynchronisationMessage},
 * associating it with an agent and capturing its current state.
 */
public class AgentSynchronisationMessage extends Entity
{
    private AgentState agentState;
    private SynchronisationMessage message;
    private String description;
    private Status status = Status.QUEUED;
    private String statusMessage;

    /**
     * States that a message passes through.
     */
    public enum Status
    {
        /**
         * The initial state for a message that has been queued and not yet
         * processed.
         */
        QUEUED(true),
        /**
         * The state for a message that has been sent to an agent for
         * processing, before the agent has responded.
         */
        PROCESSING(true),
        /**
         * Indicates the message has been successfully processed.
         */
        SUCCEEDED(false),
        /**
         * Indicates that the message failed to be sent to the agent, or the
         * agent failed to respond.  The message should be retried.
         */
        SENDING_FAILED(true),
        /**
         * Indicates that the task corresponding to the message failed and the
         * task should not be retried.
         */
        FAILED_PERMANENTLY(false);

        private boolean pending;

        Status(boolean pending)
        {
            this.pending = pending;
        }

        /**
         * Indicates if this status is for pending messages: i.e. messages that
         * should be sent (or resent) on the next synchronisation cycle.
         *
         * @return true if messages with this status are pending
         */
        public boolean isPending()
        {
            return pending;
        }

        public String getPrettyString()
        {
            return EnumUtils.toPrettyString(this);
        }

        @Override
        public String toString()
        {
            return EnumUtils.toString(this);
        }
    }

    /**
     * For hibernate.
     *
     * @see #AgentSynchronisationMessage(AgentState, com.zutubi.pulse.servercore.agent.SynchronisationMessage, String)
     */
    public AgentSynchronisationMessage()
    {
    }

    /**
     * Creates a new message for the given agent.
     *
     * @param agentState  state of the agent this message is for
     * @param message     the underlying message
     * @param description human-readable description of this message's purpose
     */
    public AgentSynchronisationMessage(AgentState agentState, SynchronisationMessage message, String description)
    {
        this.agentState = agentState;
        this.message = message;
        setDescription(description);
    }

    /**
     * @return state of the agent this message is destined for
     */
    public AgentState getAgentState()
    {
        return agentState;
    }

    public void setAgentState(AgentState agentState)
    {
        this.agentState = agentState;
    }

    /**
     * @return the underlying message that is to be sent to the agent
     */
    public SynchronisationMessage getMessage()
    {
        return message;
    }

    public void setMessage(SynchronisationMessage message)
    {
        this.message = message;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description == null ? null : StringUtils.trimmedString(description, 1023);
    }

    /**
     * @return the current status of this message
     */
    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    private String getStatusName()
    {
        return status.name();
    }

    private void setStatusName(String statusName)
    {
        status = Status.valueOf(statusName);
    }

    /**
     * If the status of this message is {@link Status#SENDING_FAILED} or
     * {@link Status#FAILED_PERMANENTLY}, this message may provide more
     * details.
     *
     * @return details for failed statuses, may include a full stack trace
     */
    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage)
    {
        this.statusMessage = statusMessage == null ? null : StringUtils.trimmedString(statusMessage, 4091);
    }

    /**
     * Should be called when this message is about to be processed: moves to
     * the {@link Status#PROCESSING} state and clears any status message.
     */
    public void startProcessing()
    {
        status = Status.PROCESSING;
        statusMessage = null;
    }

    /**
     * Applies the result of processing this message to the status and (in the
     * case of failure) the status message.
     *
     * @param result the result of processing the message
     */
    public void applyResult(SynchronisationMessageResult result)
    {
        if (result.isSuccessful())
        {
            status = Status.SUCCEEDED;
            statusMessage = null;
        }
        else
        {
            status = Status.FAILED_PERMANENTLY;
            setStatusMessage(result.getMessage());
        }
    }

    /**
     * Applies an exception caught trying to send this message.  This indicates
     * that we either could not send it or the agent could not respond.  The
     * message will be marked {@link Status#SENDING_FAILED} for retry later,
     * and the exception details stored in the status message.
     *
     * @param exception the exception raised when trying to send the message
     */
    public void applySendingException(Exception exception)
    {
        status = Status.SENDING_FAILED;
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        setStatusMessage(writer.toString());
    }
}
