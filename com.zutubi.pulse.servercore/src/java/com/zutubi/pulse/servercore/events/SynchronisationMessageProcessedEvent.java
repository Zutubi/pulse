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

package com.zutubi.pulse.servercore.events;

import com.zutubi.events.Event;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;

/**
 * Event raised when an asynchronous synchronisation message is processed by an
 * agent.
 */
public class SynchronisationMessageProcessedEvent extends Event
{
    private long agentId;
    private SynchronisationMessageResult result;

    public SynchronisationMessageProcessedEvent(Object source, long agentId, SynchronisationMessageResult result)
    {
        super(source);
        this.agentId = agentId;
        this.result = result;
    }

    public long getAgentId()
    {
        return agentId;
    }

    public SynchronisationMessageResult getResult()
    {
        return result;
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

        SynchronisationMessageProcessedEvent that = (SynchronisationMessageProcessedEvent) o;

        if (agentId != that.agentId)
        {
            return false;
        }
        if (result != null ? !result.equals(that.result) : that.result != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result1 = (int) (agentId ^ (agentId >>> 32));
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString()
    {
        return "Synchronisation Message Processed Event: agent id: " + agentId + ", message id: " + result.getMessageId() + ", successful: " + result.isSuccessful();
    }
}
