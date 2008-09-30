package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.tove.config.user.UserConfiguration;

/**
 *
 *
 */
public interface NotifyValue
{
    Comparable getValue(BuildResult result, UserConfiguration user);
}
