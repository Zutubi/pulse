package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

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

    public int getValue(BuildResult result, User user)
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
