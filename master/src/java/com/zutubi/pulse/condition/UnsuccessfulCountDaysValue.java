package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.util.Constants;

import java.util.List;

/**
 * Evaluates to the number of consecutive days that builds have been failing.
 * This is calculated as the number of consecutive milliseconds that the
 * builds have been failing divided by the milliseconds in a day, with the
 * remainder discarded.  That is, it is the number of complete, consecutive
 * 24 hour periods that the build has been failing for.  It is not tied in
 * any way to boundaries between days.
 */
public class UnsuccessfulCountDaysValue implements NotifyIntegerValue
{
    private BuildManager buildManager;

    public int getValue(BuildResult result, User user)
    {
        return getValueForBuild(result, buildManager);
    }

    public static int getValueForBuild(BuildResult result, BuildManager buildManager)
    {
        if(result != null && !result.succeeded())
        {
            Project project = result.getProject();
            BuildSpecification spec = project.getBuildSpecification(result.getSpecName().getName());
            if(spec != null)
            {
                BuildResult lastSuccess = buildManager.getLatestSuccessfulBuildResult(spec);
                List<BuildResult> firstFailure = buildManager.querySpecificationBuilds(project, spec.getPname(), null, lastSuccess == null ? 1 : lastSuccess.getNumber() + 1, -1, 0, 1, false, false);
                if(firstFailure.size() > 0)
                {
                    long failingSince = firstFailure.get(0).getStamps().getEndTime();
                    return (int) ((result.getStamps().getEndTime() - failingSince) / Constants.DAY);
                }
            }
        }

        return 0;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
