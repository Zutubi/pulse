package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;

/**
 * 
 *
 */
public class SuccessNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result, UserConfiguration user)
    {
        return result != null && result.succeeded();
    }
}
