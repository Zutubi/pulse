package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.UnaryProcedure;
import flexjson.JSON;

import java.util.List;

/**
 * JSON-encodable object representing the current state of a concrete project.
 */
public class ConcreteProjectModel extends ProjectModel
{
    private String projectName;
    private ProjectHealth health;
    private boolean built;
    private List<ProjectBuildModel> buildRows;
    private boolean canTrigger;
    private boolean canViewSource;
    private long projectId;

    public ConcreteProjectModel(ProjectsModel group, Project project, List<BuildResult> latestBuilds, final User loggedInUser, final ProjectsSummaryConfiguration configuration, final Urls urls, boolean canTrigger, boolean canViewSource)
    {
        super(group, project.getName());

        projectName = project.getName();
        projectId = project.getId();
        health = ProjectHealth.fromLatestBuilds(latestBuilds);
        built = latestBuilds.size() > 0;

        if (configuration.getBuildsPerProject() < latestBuilds.size())
        {
            latestBuilds = latestBuilds.subList(0, configuration.getBuildsPerProject());
        }
        
        buildRows = CollectionUtils.map(latestBuilds, new Mapping<BuildResult, ProjectBuildModel>()
        {
            public ProjectBuildModel map(BuildResult buildResult)
            {
                return new ProjectBuildModel(buildResult, loggedInUser, configuration, urls);
            }
        });

        this.canTrigger = canTrigger;
        this.canViewSource = canViewSource;
    }

    public String getName()
    {
        return projectName;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public ProjectHealth latestHealth()
    {
        return health;
    }

    public boolean isConcrete()
    {
        return true;
    }

    public boolean isBuilt()
    {
        return built;
    }

    @JSON
    public List<ProjectBuildModel> getBuildRows()
    {
        return buildRows;
    }

    public boolean isCanTrigger()
    {
        return canTrigger;
    }

    public boolean isCanViewSource()
    {
        return canViewSource;
    }

    public ResultState latestState()
    {
        if(buildRows.size() == 0)
        {
            return null;
        }
        else
        {
            return buildRows.get(0).getState();
        }
    }

    public int getCount(ProjectHealth health)
    {
        return latestHealth() == health ? 1 : 0;
    }

    public int getCount(ResultState state)
    {
        return state == latestState() ? 1 : 0;
    }

    public void forEach(UnaryProcedure<ProjectModel> proc)
    {
        proc.process(this);
    }
}
