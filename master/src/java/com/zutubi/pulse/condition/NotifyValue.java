package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

/**
 *
 *
 */
public interface NotifyValue
{
    Comparable getValue(BuildResult result, User user);
}
