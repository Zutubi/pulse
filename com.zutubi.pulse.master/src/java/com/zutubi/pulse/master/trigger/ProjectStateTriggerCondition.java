package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ProjectStateTriggerConditionConfiguration;
import com.zutubi.util.logging.Logger;

/**
 * A trigger condition that checks the state of another project.
 */
public class ProjectStateTriggerCondition extends TriggerConditionSupport
{
    private static final Logger LOG = Logger.getLogger(ProjectStateTriggerCondition.class);

    private BuildManager buildManager;
    private ProjectManager projectManager;

    public ProjectStateTriggerCondition(ProjectStateTriggerConditionConfiguration config)
    {
        super(config);
    }

    public boolean satisfied(Project project)
    {
        ProjectStateTriggerConditionConfiguration config = (ProjectStateTriggerConditionConfiguration) getConfig();
        ProjectConfiguration otherProjectConfig = config.getProject();
        Project otherProject = projectManager.getProject(otherProjectConfig.getProjectId(), true);
        if (otherProject == null)
        {
            LOG.warning("Project state trigger condition configured on project '" + project.getName() + "' refers to unknown project '" + otherProjectConfig.getName() + "'");
            return false;
        }

        BuildResult buildResult = buildManager.getLatestCompletedBuildResult(otherProject);
        return buildResult != null && config.getStates().contains(buildResult.getState());
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
