package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

/**
 */
public interface NotifyIntegerValue
{
    int getValue(BuildResult result, UserConfiguration user);
}
