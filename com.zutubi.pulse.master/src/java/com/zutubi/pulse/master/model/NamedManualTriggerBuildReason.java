package com.zutubi.pulse.master.model;

/**
 * Indicates that the build occurred because a user manually fired a configured trigger.
 */
public class NamedManualTriggerBuildReason extends AbstractBuildReason
{
    private String triggerName;
    private String username;

    public NamedManualTriggerBuildReason()
    {
    }

    public NamedManualTriggerBuildReason(String triggerName, String username)
    {
        this.triggerName = triggerName;
        this.username = username;
    }

    public boolean isUser()
    {
        return true;
    }

    public String getSummary()
    {
        return "'" + triggerName + "' fired by " + (username != null ? username : "anonymous");
    }

    public String getTriggerName()
    {
        return triggerName;
    }

    /**
     * @return the user and trigger names safely combined into one field, so they can be stored in
     *         one database column (like all other build reasons are)
     */
    public String getNames()
    {
        if (username == null)
        {
            return triggerName;
        }
        else
        {
            return triggerName + "/" + username;
        }
    }

    private void setNames(String names)
    {
        int index = names.indexOf('/');
        if (index < 0)
        {
            triggerName = names;
        }
        else
        {
            triggerName = names.substring(0, index);
            if (index < names.length() - 1)
            {
                username = names.substring(index + 1, names.length());
            }
            else
            {
                username = "";
            }
        }
    }
}
