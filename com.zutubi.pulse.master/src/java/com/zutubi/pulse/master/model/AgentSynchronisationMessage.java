/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.util.Constants;
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
    private static final Messages I18N = Messages.getInstance(AgentSynchronisationMessage.class);
    
    private AgentState agentState;
    private SynchronisationMessage message;
    private String description;
    private Status status = Status.QUEUED;
    private long processingTimestamp = -1;
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
        QUEUED,
        /**
         * The state for a message that has been sent to an agent for
         * processing, before the agent has responded.
         */
        PROCESSING,
        /**
         * Indicates the message has been successfully processed.
         */
        SUCCEEDED,
        /**
         * Indicates that the message possibly failed to be sent to the agent.
         * This could be due to network failure, or due to an untimely master
         * restart.  Note it is possible that the message got through before
         * the network problem/restart.  In any case the message should be
         * retried.
         */
        SENDING_FAILED,
        /**
         * Indicates that the message was sent to the agent, but no reply was
         * received in a suitable time period.  At present such messages are
         * not retried.
         */
        TIMED_OUT,
        /**
         * Indicates that the task corresponding to the message failed and the
         * task should not be retried.
         */
        FAILED_PERMANENTLY;

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

    /**
     * When this message is in the {@link Status#PROCESSING} state, indicates
     * the time at which processing began.
     * 
     * @return the time at which this message started processing, in
     *         milliseconds since the unix epoch
     */
    public long getProcessingTimestamp()
    {
        return processingTimestamp;
    }

    public void setProcessingTimestamp(long processingTimestamp)
    {
        this.processingTimestamp = processingTimestamp;
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
     * 
     * @param timestamp the time at which this processing was started, in
     *                  milliseconds since the unix epoch
     */
    public void startProcessing(long timestamp)
    {
        status = Status.PROCESSING;
        processingTimestamp = timestamp;
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

    /**
     * Moves this message to the timed out state, indicating it was processing
     * too long without a response.
     * 
     * @param timeSpentProcessing the number of milliseconds ago the message
     *                            processing started
     */
    public void timedOut(long timeSpentProcessing)
    {
        status = Status.TIMED_OUT;
        setStatusMessage(I18N.format("timed.out", timeSpentProcessing / Constants.SECOND));
    }

    /**
     * Moves this message to the sending failed state, based on a master
     * restart while the message was processing.
     */
    public void masterRestarted()
    {
        status = Status.SENDING_FAILED;
        setStatusMessage(I18N.format("master.restarted"));
    }
}
