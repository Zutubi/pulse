package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.scheduling.EventTriggerFilter;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.io.Serializable;
import java.util.Map;

/**
 * An event filter that only accepts build completed events associated with a
 * project that is listed as a dependency of the project with which the trigger
 * is configured.
 */
public class DependentBuildEventFilter implements EventTriggerFilter
{
    public static final String PARAM_PROPAGATE_STATUS   = "propagate.status";
    public static final String PARAM_PROPAGATE_VERSION   = "propagate.version";

    private ProjectManager projectManager;

    public boolean accept(Trigger trigger, Event event, TaskExecutionContext context)
    {
        if (!isBuildCompletedEvent(event))
        {
            return false;
        }

        Trigger sourceTrigger = context.getTrigger();
        
        // the project in which this trigger is configured.
        ProjectConfiguration projectConfig = projectManager.getProjectConfig(sourceTrigger.getProject(), false);
        if (projectConfig == null)
        {
            // This project is invalid or does not exist, hence we are not in a position to
            // trigger a build.  No point continuing..
            return false;
        }

        BuildCompletedEvent buildCompletedEvent = (BuildCompletedEvent) event;
        BuildResult result = buildCompletedEvent.getBuildResult();

        if (!result.succeeded())
        {
            return false;
        }

        boolean progagateStatus = getBooleanParam(trigger.getDataMap(), PARAM_PROPAGATE_STATUS, false);
        if (progagateStatus)
        {
            context.put(BuildProjectTask.PARAM_STATUS, result.getStatus());
        }

        boolean propagateVersion = getBooleanParam(trigger.getDataMap(), PARAM_PROPAGATE_VERSION, false);
        if (propagateVersion)
        {
            context.put(BuildProjectTask.PARAM_VERSION, result.getVersion());
            context.put(BuildProjectTask.PARAM_VERSION_PROPAGATED, true);
        }

        context.put(BuildProjectTask.PARAM_DEPENDENT, true);
        context.put(BuildProjectTask.PARAM_META_BUILD_ID, buildCompletedEvent.getBuildResult().getMetaBuildId());
        
        final Project builtProject = result.getProject();

        // Return true iif the triggers project contains a dependency to the built project.
        return CollectionUtils.contains(projectConfig.getDependencies().getDependencies(), new Predicate<DependencyConfiguration>()
        {
            public boolean satisfied(DependencyConfiguration dependency)
            {
                return dependency.getProject().getProjectId() == builtProject.getId();
            }
        });
    }

    private boolean isBuildCompletedEvent(Event event)
    {
        return (event instanceof BuildCompletedEvent);
    }

    private boolean getBooleanParam(Map<Serializable, Serializable> dataMap, String param, boolean defaultValue)
    {
        Boolean value = (Boolean) dataMap.get(param);
        if (value == null)
        {
            return defaultValue;
        }
        else
        {
            return value;
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

}
