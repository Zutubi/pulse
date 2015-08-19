package com.zutubi.pulse.master.rest.model;

import com.zutubi.util.StringUtils;

/**
 * Represents the result of a configuration check.
 */
public class CheckResultModel
{
    private boolean success;
    private String message;

    public CheckResultModel()
    {
        success = true;
    }

    public CheckResultModel(Exception e)
    {
        success = false;
        message = StringUtils.stringSet(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getMessage()
    {
        return message;
    }
}
