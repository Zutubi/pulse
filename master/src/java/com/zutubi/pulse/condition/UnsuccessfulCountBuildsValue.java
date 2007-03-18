package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.*;

/**
 */
public class UnsuccessfulCountBuildsValue implements NotifyIntegerValue
{
    private BuildManager buildManager;

    public int getValue(BuildResult result, User user)
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
