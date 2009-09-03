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
public class UnsuccessfulCountBuildsValue implements NotifyIntegerValue
{
    private BuildManager buildManager;

    public Comparable getValue(BuildResult result, UserConfiguration user)
    {
        return getValueForBuild(result, buildManager);
    }

    public static int getValueForBuild(BuildResult result, BuildManager buildManager)
    {
        if (result != null && !result.succeeded())
        {
            Project project = result.getProject();
            List<BuildResult> lastSuccesses = buildManager.queryBuilds(project, new ResultState[]{ ResultState.SUCCESS }, -1, result.getNumber() - 1, 0, 1, true, false);
            BuildResult lastSuccess = lastSuccesses.size() > 0 ? lastSuccesses.get(0) : null;
            return buildManager.getBuildCount(project, lastSuccess == null ? 0 : lastSuccess.getNumber(), result.getNumber());
        }

        return 0;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
