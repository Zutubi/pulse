package com.zutubi.pulse.services;

import com.zutubi.pulse.agent.Status;

/**
 * Encapsulates the state of a slave agent.
 */
public class SlaveStatus
{
    /**
     *  The agent status, as reported by the slave itself.
     */
    private Status status;
    /**
     * Id of the recipe that the slave is building, or 0 if it is not
     * currently executing a build.
     */
    private long recipeId = 0;
    /**
     * If an error occured, a detail message.
     */
    private String message = null;

    private long pingTime;

    public SlaveStatus(Status status, long recipeId)
    {
        this.status = status;
        this.recipeId = recipeId;
    }

    public SlaveStatus(Status status, String message)
    {
        this.status = status;
        this.message = message;
    }

    public SlaveStatus(Status status)
    {
        this.status = status;
    }

    public Status getStatus()
    {
        return status;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    public String getMessage()
    {
        return message;
    }

    public long getPingTime()
    {
        return pingTime;
    }

    public void setPingTime(long pingTime)
    {
        this.pingTime = pingTime;
    }
}
