package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

/**
 */
public class LiteralNotifyIntegerValue implements NotifyIntegerValue
{
    private int value;

    public LiteralNotifyIntegerValue(int value)
    {
        this.value = value;
    }

    public int getValue(BuildResult result, User user)
    {
        return value;
    }
}
