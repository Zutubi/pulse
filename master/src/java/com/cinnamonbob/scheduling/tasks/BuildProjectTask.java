package com.cinnamonbob.scheduling.tasks;

import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.TaskExecutionContext;
import com.cinnamonbob.util.logging.Logger;

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
        String spec = (String) context.getTrigger().getDataMap().get(PARAM_SPEC);
        long projectId = (Long) context.getTrigger().getDataMap().get(PARAM_PROJECT);
        Project project = projectManager.getProject(projectId);

        if (project != null)
        {
            // generate build request.
            eventManager.publish(new BuildRequestEvent(this, project, spec));
        }
        else
        {
            LOG.warning("Build project task fired for unknown project '" + projectId + "'");
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
