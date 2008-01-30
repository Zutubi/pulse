package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

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
