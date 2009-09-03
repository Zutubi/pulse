package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 */
public class LiteralNotifyIntegerValue implements NotifyIntegerValue
{
    private int value;

    public LiteralNotifyIntegerValue(int value)
    {
        this.value = value;
    }

    public Comparable getValue(BuildResult result, UserConfiguration user)
    {
        return value;
    }
}
