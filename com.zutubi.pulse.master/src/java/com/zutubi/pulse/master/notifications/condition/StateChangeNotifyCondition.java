package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * This notify condition triggers when the received build result is the first
 * successful build after a one or more failures.
 * 
 */
public class StateChangeNotifyCondition implements NotifyCondition
{
    private BuildManager buildManager;

    public StateChangeNotifyCondition()
    {
    }

    /**
     * The system build manager is required to lookup previous build results.
     *
     * @param buildManager
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public boolean satisfied(BuildResult result, UserConfiguration user)
    {
        if(result == null)
        {
            return false;
        }

        BuildResult previous = buildManager.getPreviousBuildResult(result);
        if(previous == null)
        {
            return false;
        }
        else
        {
            return previous.getState() != result.getState();
        }
    }
}
