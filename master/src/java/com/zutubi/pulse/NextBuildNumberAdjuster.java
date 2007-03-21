package com.zutubi.pulse;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;
import java.io.File;

/**
 * A startup task which checks the build directories on disk and ensure the
 * next build number will not collide (CIB-1020).
 */
public class NextBuildNumberAdjuster implements Runnable
{
    private static final Logger LOG = Logger.getLogger(NextBuildNumberAdjuster.class);

    private ProjectManager projectManager;
    private MasterConfigurationManager configurationManager;

    public void run()
    {
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        List<Project> projects = projectManager.getAllProjects();

        for (Project project : projects)
        {
            long nextNumber = project.getNextBuildNumber();
            File candidateDir = getCandidateDir(paths, project, nextNumber);
            while(candidateDir.exists())
            {
                LOG.warning("Build directory '" + candidateDir.getAbsolutePath() + "' already exists.  Skipping build number '" + nextNumber + "' for project '" + project.getName() + "'");
                // Increments and saves the new number.
                projectManager.getNextBuildNumber(project);
                candidateDir = getCandidateDir(paths, project, ++nextNumber);
            }
        }
    }

    private File getCandidateDir(MasterBuildPaths paths, Project project, long nextNumber)
    {
        return new File(paths.getProjectDir(project), MasterBuildPaths.getBuildDirName(nextNumber));
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
