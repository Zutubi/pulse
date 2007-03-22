package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

/**
 * 
 *
 */
public class ErrorNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result, User user)
    {
        return result != null && result.errored();
    }
}
