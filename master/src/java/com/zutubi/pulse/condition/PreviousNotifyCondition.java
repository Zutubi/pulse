package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

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

    public boolean satisfied(BuildResult result, User user)
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
