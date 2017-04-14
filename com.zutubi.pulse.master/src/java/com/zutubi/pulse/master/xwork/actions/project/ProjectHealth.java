/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
