package com.zutubi.pulse.servercore.services;

import com.zutubi.pulse.servercore.agent.PingStatus;

/**
 * Encapsulates the state of a slave agent.
 */
public class SlaveStatus
{
    public static final long NO_RECIPE = 0;

    /**
     *  The agent status, as reported by the slave itself.
     */
    private PingStatus status;
    /**
     * Id of the recipe that the slave is building, or 0 if it is not
     * currently executing a build.
     */
    private long recipeId = NO_RECIPE;
    /**
     * If true, this is the first status request the agent has answered since
     * it booted.  Used to detect agent bounces between pings (CIB-1141).
     */
    private boolean first = false;
    /**
     * If an error occured, a detail message.
     */
    private String message = null;

    public SlaveStatus(PingStatus status, long recipeId, boolean first)
    {
        this.status = status;
        this.recipeId = recipeId;
        this.first = first;
    }

    public SlaveStatus(PingStatus status, String message)
    {
        this.status = status;
        this.message = message;
    }

    public SlaveStatus(PingStatus status)
    {
        this.status = status;
    }

    public PingStatus getStatus()
    {
        return status;
    }

    public long getRecipeId()
    {
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

        SlaveStatus status1 = (SlaveStatus) o;

        if (first != status1.first)
        {
            return false;
        }
        if (recipeId != status1.recipeId)
        {
            return false;
        }
        if (message != null ? !message.equals(status1.message) : status1.message != null)
        {
            return false;
        }
        if (status != status1.status)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = status.hashCode();
        result = 31 * result + (int) (recipeId ^ (recipeId >>> 32));
        result = 31 * result + (first ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        String result = status.getPrettyString();
        if(recipeId != 0)
        {
            result += ", building " + recipeId;
        }

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
