package com.zutubi.pulse.servercore.services;

import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.NotEqualsPredicate;

import java.util.HashMap;
import java.util.Map;

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
     * If true, this is the first status request the agent has answered since
     * it booted.  Used to detect agent bounces between pings (CIB-1141).
     */
    private boolean first = false;
    /**
     * If an error occurred, a detail message.
     */
    private String message = null;

    public HostStatus(Map<Long, Long> agentHandleToRecipeId, boolean first)
    {
        this.status = CollectionUtils.contains(agentHandleToRecipeId.values(), new NotEqualsPredicate<Long>(NO_RECIPE)) ? PingStatus.BUILDING : PingStatus.IDLE;
        this.agentHandleToRecipeId = agentHandleToRecipeId;
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
