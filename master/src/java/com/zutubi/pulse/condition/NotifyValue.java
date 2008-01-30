package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

/**
 *
 *
 */
public interface NotifyValue
{
    Comparable getValue(BuildResult result, UserConfiguration user);
}
