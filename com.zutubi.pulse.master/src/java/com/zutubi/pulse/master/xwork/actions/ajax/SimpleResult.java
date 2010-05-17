package com.zutubi.pulse.master.xwork.actions.ajax;

/**
 * A simple result object for generating a JSON tuple with a success flag and
 * optional detail message.
 */
public class SimpleResult
{
    private boolean success;
    private String detail;

    public SimpleResult(boolean success, String detail)
    {
        this.success = success;
        this.detail = detail;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getDetail()
    {
        return detail;
    }
}
