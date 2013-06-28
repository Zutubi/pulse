package com.zutubi.pulse.master.xwork.actions.project;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectResponsibility;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.pulse.master.tove.config.user.ProjectsSummaryConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;
import com.zutubi.util.UnaryProcedure;
import flexjson.JSON;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.ACTION_TRIGGER;

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
    private long projectId;
    private List<TriggerModel> triggers;

    public ConcreteProjectModel(ProjectsModel group, Project project, List<BuildResult> latestBuilds, final User loggedInUser, final ProjectsSummaryConfiguration configuration, final Urls urls, Set<String> availableActions, ProjectHealth projectHealth, ProjectMonitoring monitoring)
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
        buildRows = newArrayList(transform(latestBuilds, new Function<BuildResult, ProjectBuildModel>()
        {
            public ProjectBuildModel apply(BuildResult buildResult)
            {
                return new ProjectBuildModel(buildResult, configuration, urls, absoluteTimestamps);
            }
        }));

        if (availableActions.contains(ACTION_TRIGGER))
        {
            triggers = Lists.transform(TriggerUtils.getTriggers(project.getConfig(), ManualTriggerConfiguration.class), new Function<ManualTriggerConfiguration, TriggerModel>()
            {
                public TriggerModel apply(ManualTriggerConfiguration input)
                {
                    return new TriggerModel(input);
                }
            });
        }
        else
        {
            triggers = Collections.emptyList();
        }

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

    @JSON
    public List<TriggerModel> getTriggers()
    {
        return triggers;
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
