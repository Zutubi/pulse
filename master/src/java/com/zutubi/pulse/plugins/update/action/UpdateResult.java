package com.zutubi.pulse.plugins.update.action;

/**
 */
public interface UpdateResult
{
    enum Status
    {
        ABORTED,
        ERROR,
        SUCCESS
    }

    Status getStatus();
    String getMessage();
}
