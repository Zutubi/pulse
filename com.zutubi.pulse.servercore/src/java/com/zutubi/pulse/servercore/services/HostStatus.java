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

package com.zutubi.pulse.servercore.services;

import com.zutubi.pulse.servercore.agent.PingStatus;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.any;

/**
 * Encapsulates the state of a host.
 */
public class HostStatus
{
    /**
     * Recipe id used to mean there is no recipe running.
     */
    public static final long NO_RECIPE = 0;

    /**
     *  The host status, as reported by the host itself.
     */
    private PingStatus status;
    /**
     * Mapping from agent handle to the id of the recipe that the agent is
     * building.  The agent handle may map to either nothing, or 0 if it is not
     * currently executing a build.
     */
    private Map<Long, Long> agentHandleToRecipeId = new HashMap<Long, Long>();
    /**
     * Amount of free disk space on the volume where the agent's data directory is, in bytes.
     * May be 0 if the free space could not be determined.
     */
    private long freeDiskSpace;
    /**
     * If true, this is the first status request the agent has answered since
     * it booted.  Used to detect agent bounces between pings (CIB-1141).
     */
    private boolean first = false;
    /**
     * If an error occurred, a detail message.
     */
    private String message = null;

    public HostStatus(Map<Long, Long> agentHandleToRecipeId, long freeDiskSpace, boolean first)
    {
        this.status = any(agentHandleToRecipeId.values(), not(equalTo(NO_RECIPE))) ? PingStatus.BUILDING : PingStatus.IDLE;
        this.agentHandleToRecipeId = agentHandleToRecipeId;
        this.freeDiskSpace = freeDiskSpace;
        this.first = first;
    }

    public HostStatus(PingStatus status, boolean first)
    {
        this.status = status;
        this.first = first;
    }

    public HostStatus(PingStatus status, String message)
    {
        this.status = status;
        this.message = message;
    }

    public HostStatus(PingStatus status)
    {
        this.status = status;
    }

    public PingStatus getStatus()
    {
        return status;
    }

    public PingStatus getStatus(long agentHandle)
    {
        if (status == PingStatus.BUILDING)
        {
            long recipeId = getRecipeId(agentHandle);
            return recipeId == NO_RECIPE ? PingStatus.IDLE : PingStatus.BUILDING;
        }
        else
        {
            return status;
        }
    }

    public long getRecipeId(long agentHandle)
    {
        Long recipeId = agentHandleToRecipeId.get(agentHandle);
        if (recipeId == null)
        {
            recipeId = NO_RECIPE;
        }

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

        HostStatus that = (HostStatus) o;

        if (first != that.first)
        {
            return false;
        }
        if (agentHandleToRecipeId != null ? !agentHandleToRecipeId.equals(that.agentHandleToRecipeId) : that.agentHandleToRecipeId != null)
        {
            return false;
        }
        if (message != null ? !message.equals(that.message) : that.message != null)
        {
            return false;
        }
        if (status != that.status)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (agentHandleToRecipeId != null ? agentHandleToRecipeId.hashCode() : 0);
        result = 31 * result + (first ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        String result = status.getPrettyString();
        result += ", recipes " + agentHandleToRecipeId;

        if(first)
        {
            result += ", (first)";
        }

        if(message != null)
        {
            result += ": '" + message + "'";
        }

        return result;
    }
}
