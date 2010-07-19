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
