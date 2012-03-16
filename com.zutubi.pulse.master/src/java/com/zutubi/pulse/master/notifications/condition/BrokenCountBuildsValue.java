package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

import java.util.List;

/**
 * Evaluates to the number of consecutive unsuccessful builds up to and
 * including the given build.
 */
public class BrokenCountBuildsValue implements NotifyIntegerValue
{
    private BuildManager buildManager;

    public Comparable getValue(BuildResult result, UserConfiguration user)
    {
        return getValueForBuild(result, buildManager);
    }

    public static int getValueForBuild(BuildResult result, BuildManager buildManager)
    {
        if (result != null && !result.healthy())
        {
            Project project = result.getProject();
            List<BuildResult> lastHealthies = buildManager.queryBuilds(project, ResultState.getHealthyStates(), -1, result.getNumber() - 1, 0, 1, true, false);
            BuildResult lastHealthy = lastHealthies.size() > 0 ? lastHealthies.get(0) : null;
            return buildManager.getBuildCount(project, lastHealthy == null ? 0 : lastHealthy.getNumber(), result.getNumber());
        }

        return 0;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
