package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.*;

/**
 * Evaluates to the number of consecutive unsuccessful builds up to and
 * including the given build.
 */
public class UnsuccessfulCountBuildsValue implements NotifyIntegerValue
{
    private BuildManager buildManager;

    public int getValue(BuildResult result, User user)
    {
        return getValueForBuild(result, buildManager);
    }

    public static int getValueForBuild(BuildResult result, BuildManager buildManager)
    {
        if (result != null && !result.succeeded())
        {
            Project project = result.getProject();
            BuildSpecification spec = project.getBuildSpecification(result.getSpecName().getName());
            if(spec != null)
            {
                BuildResult lastSuccess = buildManager.getLatestSuccessfulBuildResult(spec);
                return buildManager.getBuildCount(spec, lastSuccess == null ? 0 : lastSuccess.getNumber(), result.getNumber());
            }
        }

        return 0;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
