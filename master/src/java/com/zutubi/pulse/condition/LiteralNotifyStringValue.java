package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

/**
 *
 *
 */
public class LiteralNotifyStringValue implements NotifyStringValue
{
    private String value;

    public LiteralNotifyStringValue(String value)
    {
        this.value = value;
    }

    public String getValue(BuildResult result, User user)
    {
        return value;
    }
}
