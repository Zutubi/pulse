package com.zutubi.pulse.condition;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.*;

import java.util.List;

/**
 * Evaluates to the number of consecutive unsuccessful builds up to and
 * including the given build.
 */
public class UnsuccessfulCountBuildsValue implements NotifyIntegerValue
{
    private BuildManager buildManager;

    public Comparable getValue(BuildResult result, User user)
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
                List<BuildResult> lastSuccesses = buildManager.querySpecificationBuilds(project, spec.getPname(), new ResultState[]{ ResultState.SUCCESS }, -1, result.getNumber() - 1, 0, 1, true, false);
                BuildResult lastSuccess = lastSuccesses.size() > 0 ? lastSuccesses.get(0) : null;
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
