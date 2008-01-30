package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

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

    public String getValue(BuildResult result, UserConfiguration user)
    {
        return value;
    }
}
