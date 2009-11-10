package com.zutubi.pulse.master.cleanup.requests;

import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.model.BuildCleanupOptions;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.List;
import java.util.Map;

/**
 * A request to cleanup a projects builds.  Which builds and what specifically
 * are cleaned up is dependent upon the projects configured cleanup rules.
 */
public class ProjectCleanupRequest extends EntityCleanupRequest
{
    private BuildResultDao buildResultDao;
    private BuildManager buildManager;

    private Project project;

    public ProjectCleanupRequest(Project project)
    {
        super(project);
        this.project = project;
    }

    public void run()
    {
        ProjectConfiguration projectConfig = project.getConfig();
        @SuppressWarnings({"unchecked"})
        Map<String, CleanupConfiguration> cleanupConfigs = (Map<String, CleanupConfiguration>) projectConfig.getExtensions().get(MasterConfigurationRegistry.EXTENSION_PROJECT_CLEANUP);

        if (cleanupConfigs != null)
        {
            // if cleanup rules are specified.  Maybe we should always have at least an empty map?
            for (CleanupConfiguration rule : cleanupConfigs.values())
            {
                List<BuildResult> oldBuilds = rule.getMatchingResults(project, buildResultDao);

                for (BuildResult build : oldBuilds)
                {
                    BuildCleanupOptions options = new BuildCleanupOptions(false);
                    if (rule.isCleanupAll())
                    {
                        options = new BuildCleanupOptions(true);
                    }
                    else
                    {
                        if (rule.getWhat() != null)
                        {
                            options = new BuildCleanupOptions(rule.getWhat());
                        }
                    }

                    buildManager.cleanup(build, options);
                }
            }
        }
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }
}
