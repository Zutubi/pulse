package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 *
 *
 */
public interface NotifyValue
{
    Comparable getValue(BuildResult result, UserConfiguration user);
}
