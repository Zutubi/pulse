package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

/**
 */
public interface NotifyIntegerValue
{
    int getValue(BuildResult result, User user);
}
