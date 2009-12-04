package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.BuildCompletedTriggerConfiguration;

import java.util.List;

/**
 * A filter that will only allow triggers for builds that complete in
 * certain states.
 */
public class BuildCompletedEventFilter implements EventTriggerFilter
{
    public boolean accept(Trigger trigger, Event event, TaskExecutionContext context)
    {
        BuildCompletedEvent buildCompletedEvent = (BuildCompletedEvent) event;
        BuildCompletedTriggerConfiguration config = (BuildCompletedTriggerConfiguration) trigger.getConfig();
        boolean accept = !buildCompletedEvent.getBuildResult().isPersonal() && checkProject(config, buildCompletedEvent) && checkState(config, buildCompletedEvent);
        if (accept)
        {
            // Pass some information to the task.
            if (config.isPropagateRevision())
            {
                // Copy the revision: we don't want to share the persistent instance.
                context.put(BuildProjectTask.PARAM_REVISION, new Revision(buildCompletedEvent.getBuildResult().getRevision().getRevisionString()));
                context.put(BuildProjectTask.PARAM_REPLACEABLE, config.isSupercedeQueued());
            }
            if (config.isPropagateStatus())
            {
                context.put(BuildProjectTask.PARAM_STATUS, buildCompletedEvent.getBuildResult().getStatus());
            }
            if (config.isPropagateVersion())
            {
                context.put(BuildProjectTask.PARAM_VERSION, buildCompletedEvent.getBuildResult().getVersion());
                context.put(BuildProjectTask.PARAM_VERSION_PROPAGATED, true);
            }
        }
        return accept;
    }

    private boolean checkProject(BuildCompletedTriggerConfiguration config, BuildCompletedEvent event)
    {
        ProjectConfiguration projectConfig = config.getProject();
        return projectConfig == null || projectConfig.getProjectId() == event.getBuildResult().getProject().getId();
    }

    private boolean checkState(BuildCompletedTriggerConfiguration config, BuildCompletedEvent event)
    {
        List<ResultState> states = config.getStates();
        if(states == null || states.isEmpty())
        {
            return true;
        }

        ResultState state = event.getBuildResult().getState();
        return states.contains(state);
    }
}
