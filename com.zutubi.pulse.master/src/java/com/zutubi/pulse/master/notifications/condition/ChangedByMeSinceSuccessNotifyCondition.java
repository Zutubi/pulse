package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * A condition that is true if there have been changes between the last
 * successful build and this build.
 */
public class ChangedByMeSinceSuccessNotifyCondition extends AbstractChangedByMeSinceNotifyCondition
{
    @Override
    public boolean satisfied(final BuildResult result, final UserConfiguration user)
    {
        return satisfied(result, user, new ResultState[]{ ResultState.SUCCESS });
    }
}