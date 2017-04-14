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

package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.List;

/**
 * A startup task which checks the build directories on disk and ensure the
 * next build number will not collide (CIB-1020).
 */
public class NextBuildNumberAdjusterStartupTask implements StartupTask
{
    private static final Logger LOG = Logger.getLogger(NextBuildNumberAdjusterStartupTask.class);

    private ProjectManager projectManager;
    private MasterConfigurationManager configurationManager;

    public void execute()
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        List<Project> projects = projectManager.getProjects(true);

        for (Project project : projects)
        {
            long nextNumber = projectManager.updateAndGetNextBuildNumber(project, false);
            File candidateDir = getCandidateDir(paths, project, nextNumber);
            while(candidateDir.exists())
            {
                LOG.warning("Build directory '" + candidateDir.getAbsolutePath() + "' already exists.  Skipping build number '" + nextNumber + "' for project '" + project.getId() + "'");
                // Allocates the new number.
                projectManager.updateAndGetNextBuildNumber(project, true);
                candidateDir = getCandidateDir(paths, project, ++nextNumber);
            }
        }
    }

    private File getCandidateDir(MasterBuildPaths paths, Project project, long nextNumber)
    {
        return new File(paths.getProjectDir(project), MasterBuildPaths.getBuildDirName(nextNumber));
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
