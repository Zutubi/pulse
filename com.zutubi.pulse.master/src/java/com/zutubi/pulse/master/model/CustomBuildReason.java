package com.zutubi.pulse.master.model;

/**
 * The build was triggered with a custom reason message.
 */
public class CustomBuildReason extends AbstractBuildReason
{
    private String summary;

    public CustomBuildReason()
    {
    }

    public CustomBuildReason(String summary)
    {
        this.summary = summary;
    }

    public boolean isUser()
    {
        return true;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }
}
