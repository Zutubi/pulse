package com.zutubi.pulse.master.cleanup.requests;

import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.cleanup.config.CleanupWhat;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.BuildCleanupOptions;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.core.dependency.DependencyManager;

import java.util.LinkedList;
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
    private DependencyManager dependencyManager;

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
            List<CleanupConfiguration> rules = new LinkedList<CleanupConfiguration>(cleanupConfigs.values());

            for (CleanupConfiguration rule : rules)
            {
                List<BuildResult> oldBuilds = rule.getMatchingResults(project, buildResultDao, dependencyManager);

                for (BuildResult build : oldBuilds)
                {
                    BuildCleanupOptions options = new BuildCleanupOptions();
                    if (rule.isCleanupAll())
                    {
                        options.setCleanBuildArtifacts(true);
                        options.setCleanWorkDir(true);
                        options.setCleanDatabase(true);
                        options.setCleanRepositoryArtifacts(true);
                    }
                    else
                    {
                        if (rule.getWhat() != null)
                        {
                            if (rule.getWhat().contains(CleanupWhat.WORKING_DIRECTORIES_ONLY))
                            {
                                options.setCleanWorkDir(true);
                            }

                            if (rule.getWhat().contains(CleanupWhat.BUILD_ARTIFACTS))
                            {
                                options.setCleanBuildArtifacts(true);
                            }

                            if (rule.getWhat().contains(CleanupWhat.REPOSITORY_ARTIFACTS))
                            {
                                options.setCleanRepositoryArtifacts(true);
                            }
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

    public void setDependencyManager(DependencyManager dependencyManager)
    {
        this.dependencyManager = dependencyManager;
    }
}
