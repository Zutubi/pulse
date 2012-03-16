package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;

/**
 * The Projects Health is a concept used to indicate the current state
 * of a project based on whether or not a projects latest completed
 * build was successful, was successful with warnings, or failed.
 */
public enum ProjectHealth
{
    // NOTE:  the order matters.  More 'severe' healths should come later.

    /**
     * The health of the project can not be determined, or has not been determined.
     */
    UNKNOWN,

    /**
     * The latest completed build has been successful.
     */
    OK,

    /**
     * The latest completed build has been successful with warnings.
     */
    WARNINGS,

    /**
     * The latest completed build was not successful.
     */
    BROKEN;

    /**
     * Determine the health of the specified project.
     *
     * @param buildManager  the build manager provides access to the resources needed to
     *                      determine the projects health
     * @param project       the project whose health is being determined.
     * @return  the projects health.
     */
    public static ProjectHealth getHealth(BuildManager buildManager, Project project)
    {
        BuildResult latestCompleted = buildManager.getLatestCompletedBuildResult(project);
        return getHealth(latestCompleted);
    }

    /**
     * Determine the health of a project based on its latest completed build.
     *
     * @param latestCompleted   the latest completed build for the project whose
     * health is being determined.
     * 
     * @return  the projects health.
     */
    public static ProjectHealth getHealth(BuildResult latestCompleted)
    {
        if (latestCompleted == null)
        {
            return UNKNOWN;
        }
        else if (latestCompleted.succeeded())
        {
            return OK;
        }
        else if (latestCompleted.warned())
        {
            return WARNINGS;
        }
        else
        {
            return BROKEN;
        }
    }
}
