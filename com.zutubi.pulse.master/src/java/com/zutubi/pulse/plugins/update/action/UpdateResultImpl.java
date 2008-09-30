package com.zutubi.pulse.plugins.update.action;

/**
 */
public class UpdateResultImpl implements UpdateResult
{
    private Status status;
    private String message;


    public UpdateResultImpl(Status status)
    {
        this.status = status;
    }

    public UpdateResultImpl(Status status, String message)
    {
        this.status = status;
        this.message = message;
    }

    public Status getStatus()
    {
        return status;
    }

    public String getMessage()
    {
        return message;
    }
}
