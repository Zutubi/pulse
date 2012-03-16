package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.util.Constants;

import java.util.List;

/**
 * Evaluates to the number of consecutive days that builds have been failing.
 * This is calculated as the number of consecutive milliseconds that the
 * builds have been failing divided by the milliseconds in a day, with the
 * remainder discarded.  That is, it is the number of complete, consecutive
 * 24 hour periods that the build has been failing for.  It is not tied in
 * any way to boundaries between days.
 */
public class BrokenCountDaysValue implements NotifyIntegerValue
{
    private BuildManager buildManager;

    public Comparable getValue(BuildResult result, UserConfiguration user)
    {
        return getValueForBuild(result, buildManager);
    }

    public static int getValueForBuild(BuildResult result, BuildManager buildManager)
    {
        if(result != null && !result.healthy())
        {
            Project project = result.getProject();
            List<BuildResult> lastlastHealthies = buildManager.queryBuilds(project, ResultState.getHealthyStates(), -1, result.getNumber() - 1, 0, 1, true, false);
            BuildResult lastHealthy = lastlastHealthies.size() > 0 ? lastlastHealthies.get(0) : null;
            List<BuildResult> firstBroken = buildManager.queryBuilds(project, null, lastHealthy == null ? 1 : lastHealthy.getNumber() + 1, -1, 0, 1, false, false);
            if(firstBroken.size() > 0)
            {
                long failingSince = firstBroken.get(0).getStamps().getEndTime();
                return (int) ((result.getStamps().getEndTime() - failingSince) / Constants.DAY);
            }
        }

        return 0;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
