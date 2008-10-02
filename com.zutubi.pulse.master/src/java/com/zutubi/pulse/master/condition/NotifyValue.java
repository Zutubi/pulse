package com.zutubi.pulse.master.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.tove.config.user.UserConfiguration;

/**
 *
 *
 */
public interface NotifyValue
{
    Comparable getValue(BuildResult result, UserConfiguration user);
}
