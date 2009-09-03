package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 */
public class PreviousNotifyIntegerValue implements NotifyIntegerValue
{
    private NotifyIntegerValue delegate;
    private BuildManager buildManager;

    public PreviousNotifyIntegerValue(NotifyIntegerValue delegate)
    {
        this.delegate = delegate;
    }

    public Comparable getValue(BuildResult result, UserConfiguration user)
    {
        BuildResult previous = buildManager.getPreviousBuildResult(result);
        return delegate.getValue(previous, user);
    }

    public NotifyIntegerValue getDelegate()
    {
        return delegate;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
