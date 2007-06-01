package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.selectedBuildsConditionConfig")
public class SelectedBuildsConditionConfiguration extends SubscriptionConditionConfiguration
{
    private boolean unsuccessful;
    private boolean includeChanges;
    private boolean includeChangesByMe;
    private boolean statusChange;

    public boolean getUnsuccessful()
    {
        return unsuccessful;
    }

    public void setUnsuccessful(boolean unsuccessful)
    {
        this.unsuccessful = unsuccessful;
    }

    public boolean getIncludeChanges()
    {
        return includeChanges;
    }

    public void setIncludeChanges(boolean includeChanges)
    {
        this.includeChanges = includeChanges;
    }

    public boolean getIncludeChangesByMe()
    {
        return includeChangesByMe;
    }

    public void setIncludeChangesByMe(boolean includeChangesByMe)
    {
        this.includeChangesByMe = includeChangesByMe;
    }

    public boolean getStatusChange()
    {
        return statusChange;
    }

    public void setStatusChange(boolean statusChange)
    {
        this.statusChange = statusChange;
    }
}
