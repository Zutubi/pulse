package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 */
public class PreviousNotifyCondition implements NotifyCondition
{
    private NotifyCondition delegate;
    private BuildManager buildManager;
    
    public PreviousNotifyCondition(NotifyCondition delegate)
    {
        this.delegate = delegate;
    }

    public boolean satisfied(BuildResult result, UserConfiguration user)
    {
        if(result == null)
        {
            return false;
        }

        BuildResult previous = buildManager.getPreviousBuildResult(result);
        return delegate.satisfied(previous, user);
    }

    public NotifyCondition getDelegate()
    {
        return delegate;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
