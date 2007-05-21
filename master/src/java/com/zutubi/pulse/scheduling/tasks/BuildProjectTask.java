package com.zutubi.pulse.scheduling.tasks;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.TriggerBuildReason;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.util.logging.Logger;

import java.io.Serializable;
import java.util.Map;

/**
 * <class-comment/>
 */
public class BuildProjectTask implements Task
{
    public static final String PARAM_SPEC = "spec";
    public static final String PARAM_FORCE = "force";

    private static final Logger LOG = Logger.getLogger(BuildProjectTask.class);

    private ProjectManager projectManager;

    public void execute(TaskExecutionContext context)
    {
        Trigger trigger = context.getTrigger();
        Map<Serializable, Serializable> dataMap = trigger.getDataMap();
        long projectId = trigger.getProject();
        boolean force = dataMap.containsKey(PARAM_FORCE);

        ProjectConfiguration project = projectManager.getProjectConfig(projectId);
        if (project != null)
        {
            // generate build request.
            projectManager.triggerBuild(project, new TriggerBuildReason(trigger.getName()), null, force);
        }
        else
        {
            LOG.warning("Build project task fired for unknown project '" + projectId + "' (trigger '" + trigger.getName() + "')");
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
