/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.condition;

import com.zutubi.pulse.condition.NotifyCondition;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

public class NotNotifyCondition implements NotifyCondition
{
    private NotifyCondition condition;

    public NotNotifyCondition(NotifyCondition condition)
    {
        this.condition = condition;
    }

    public boolean satisfied(BuildResult result, User user)
    {
        return !condition.satisfied(result, user);
    }

    public NotifyCondition getCondition()
    {
        return condition;
    }
}