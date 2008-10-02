package com.zutubi.pulse.master.scheduling.tasks;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerBuildReason;
import com.zutubi.pulse.master.scheduling.Task;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.logging.Logger;

/**
 * A trigger task which triggers a project build.
 */
public class BuildProjectTask implements Task
{
    /**
     * The fixed revision to be built, if not present the revision will float
     * to the latest (Revision, optional).
     */
    public static final String PARAM_REVISION  = "revision";
    /**
     * Indicates if the raised build request should be replaceable by later
     * requests from the same source while queued (Boolean, optional, defaults
     * to true).
     */
    public static final String PARAM_REPLACEABLE = "replaceable";

    private static final Logger LOG = Logger.getLogger(BuildProjectTask.class);

    private ProjectManager projectManager;

    public void execute(TaskExecutionContext context)
    {
        Trigger trigger = context.getTrigger();
        long projectId = trigger.getProject();
        Revision revision = (Revision) context.get(PARAM_REVISION);

        Boolean replaceableValue = (Boolean) context.get(PARAM_REPLACEABLE);
        boolean replaceable = replaceableValue == null || replaceableValue;

        ProjectConfiguration project = projectManager.getProjectConfig(projectId, false);
        if (project != null)
        {
            // generate build request.
            projectManager.triggerBuild(project, new TriggerBuildReason(trigger.getName()), revision, getSource(trigger), replaceable, false);
        }
        else
        {
            LOG.warning("Build project task fired for unknown project '" + projectId + "' (trigger '" + trigger.getName() + "')");
        }
    }

    private static String getSource(Trigger trigger)
    {
        return "trigger '" + trigger.getName() + "'";
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
