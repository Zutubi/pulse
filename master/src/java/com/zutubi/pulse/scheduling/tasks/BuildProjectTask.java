package com.zutubi.pulse.scheduling.tasks;

import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.TriggerBuildReason;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.util.logging.Logger;

/**
 * <class-comment/>
 */
public class BuildProjectTask implements Task
{
    public static final String PARAM_SPEC = "spec";
    public static final String PARAM_PROJECT = "project";

    private static final Logger LOG = Logger.getLogger(BuildProjectTask.class);

    private EventManager eventManager;
    private ProjectManager projectManager;

    public void execute(TaskExecutionContext context)
    {
        Trigger trigger = context.getTrigger();
        String spec = (String) trigger.getDataMap().get(PARAM_SPEC);
        long projectId = (Long)trigger.getDataMap().get(PARAM_PROJECT);
        Project project = projectManager.getProject(projectId);

        if (project != null)
        {
            // generate build request.
            eventManager.publish(new BuildRequestEvent(this, new TriggerBuildReason(trigger.getName()), project, spec));
        }
        else
        {
            LOG.warning("Build project task fired for unknown project '" + projectId + "' (trigger '" + trigger.getName() + "')");
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
