package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildResult;

import java.util.List;

/**
 */
public enum ProjectHealth
{
    // NOTE:  the order matters.  More 'severe' healths should come later.
    UNKNOWN,
    OK,
    WARNINGS,
    BROKEN;

    public static ProjectHealth fromLatestBuilds(List<BuildResult> builds)
    {
        BuildResult latestCompleted = null;
        for(BuildResult r: builds)
        {
            if(r.completed())
            {
                latestCompleted = r;
                break;
            }
        }

        return fromLatestBuild(latestCompleted);
    }
    
    public static ProjectHealth fromLatestBuild(BuildResult build)
    {
        if (build == null)
        {
            return UNKNOWN;
        }
        else if (build.succeeded())
        {
            if (build.getWarningFeatureCount() > 0)
            {
                return WARNINGS;
            }
            else
            {
                return OK;
            }
        }
        else
        {
            return BROKEN;
        }
    }

}
