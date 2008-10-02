package com.zutubi.pulse.master.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.tove.config.user.UserConfiguration;

public class TrueNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result, UserConfiguration user)
    {
        return true;
    }

}