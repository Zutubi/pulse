package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectResponsibility;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryProcedure;
import flexjson.JSON;

import java.util.List;
import java.util.Set;

import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.*;

/**
 * JSON-encodable object representing the current state of a concrete project.
 */
public class ConcreteProjectModel extends ProjectModel
{
    private static final int MAX_COMMENT_LENGTH = 60;

    private String projectName;
    private ProjectHealth health = ProjectHealth.UNKNOWN;
    private ProjectMonitoring monitoring;
    private String responsibleMessage;
    private String responsibleComment;
    private boolean built;
    private List<ProjectBuildModel> buildRows;
    private boolean canTrigger;
    private boolean prompt;
    private boolean canRebuild;
    private boolean canViewSource;
    private long projectId;

    public ConcreteProjectModel(ProjectsModel group, Project project, List<BuildResult> latestBuilds, final User loggedInUser, final ProjectsSummaryConfiguration configuration, final Urls urls, boolean prompt, Set<String> availableActions, ProjectHealth projectHealth, ProjectMonitoring monitoring)
    {
        super(group, project.getName());

        projectName = project.getName();
        projectId = project.getId();

        ProjectResponsibility responsibility = project.getResponsibility();
        if (responsibility != null)
        {
            responsibleMessage = responsibility.getMessage(loggedInUser);
            responsibleComment = StringUtils.trimmedString(responsibility.getComment(), MAX_COMMENT_LENGTH);
        }

        built = latestBuilds.size() > 0;

        if (configuration.getBuildsPerProject() < latestBuilds.size())
        {
            latestBuilds = latestBuilds.subList(0, configuration.getBuildsPerProject());
        }

        final boolean absoluteTimestamps = loggedInUser != null && loggedInUser.getPreferences().getDefaultTimestampDisplay() == UserPreferencesConfiguration.TimestampDisplay.ABSOLUTE;
        buildRows = CollectionUtils.map(latestBuilds, new Mapping<BuildResult, ProjectBuildModel>()
        {
            public ProjectBuildModel map(BuildResult buildResult)
            {
                return new ProjectBuildModel(buildResult, configuration, urls, absoluteTimestamps);
            }
        });

        this.prompt = prompt;
        canTrigger = availableActions.contains(ACTION_TRIGGER);
        canRebuild = availableActions.contains(ACTION_REBUILD);
        canViewSource = availableActions.contains(ACTION_VIEW_SOURCE);

        this.health = projectHealth;
        this.monitoring = monitoring;
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

    public ProjectMonitoring getMonitoring()
    {
        return monitoring;
    }

    public String getResponsibleMessage()
    {
        return responsibleMessage;
    }

    public String getResponsibleComment()
    {
        return responsibleComment;
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

    public boolean isPrompt()
    {
        return prompt;
    }

    public boolean isCanRebuild()
    {
        return canRebuild;
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
        proc.run(this);
    }
}
