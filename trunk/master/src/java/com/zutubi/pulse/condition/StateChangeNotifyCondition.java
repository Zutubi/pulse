package com.zutubi.pulse.condition;

import com.zutubi.pulse.condition.NotifyCondition;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

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

    public boolean satisfied(BuildResult result, User user)
    {
        // retrieve the previous result. If it was a failure, then this condition is satisfied.
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
