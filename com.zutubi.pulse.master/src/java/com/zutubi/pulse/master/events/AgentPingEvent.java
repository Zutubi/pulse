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

package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.servercore.agent.PingStatus;

/**
 * Event raised when an agent's status should be updated as the result of a
 * host ping.
 */
public class AgentPingEvent extends AgentEvent
{
    private PingStatus pingStatus;
    private long recipeId;
    private long freeDiskSpace;
    private boolean first;
    private String message;

    public AgentPingEvent(Object source, Agent agent, PingStatus pingStatus)
    {
        this(source, agent, pingStatus, 0, 0, false, null);
    }

    public AgentPingEvent(Object source, Agent agent, PingStatus pingStatus, String message)
    {
        this(source, agent, pingStatus, 0, 0, false, message);
    }

    public AgentPingEvent(Object source, Agent agent, PingStatus pingStatus, long recipeId, long freeDiskSpace, boolean first, String message)
    {
        super(source, agent);
        this.pingStatus = pingStatus;
        this.recipeId = recipeId;
        this.freeDiskSpace = freeDiskSpace;
        this.first = first;
        this.message = message;
    }

    public PingStatus getPingStatus()
    {
        return pingStatus;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    public long getFreeDiskSpace()
    {
        return freeDiskSpace;
    }

    public boolean isFirst()
    {
        return first;
    }

    public String getMessage()
    {
        return message;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Agent Ping Event");
        if (getAgent() != null)
        {
            builder.append(": ").append(getAgent().getName()).append(", ").append(pingStatus);
        }
        return builder.toString();
    }
}