package com.zutubi.pulse.master.model;

/**
 * The build was triggered via the remote API with no additional reason specified.
 */
public class RemoteTriggerBuildReason extends AbstractBuildReason
{
    private String username;

    public RemoteTriggerBuildReason()
    {
    }

    public RemoteTriggerBuildReason(String username)
    {
        this.username = username;
    }

    public boolean isUser()
    {
        return true;
    }

    public String getSummary()
    {
        return "trigger via remote api by " + username;
    }

    public String getUsername()
    {
        return username;
    }

    /**
     * Used by hibernate.
     *
     * @param username property value.
     */
    private void setUsername(String username)
    {
        this.username = username;
    }
}
