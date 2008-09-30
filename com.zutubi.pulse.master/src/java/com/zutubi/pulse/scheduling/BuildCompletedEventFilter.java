package com.zutubi.pulse.scheduling;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;

import java.io.Serializable;
import java.util.Map;

/**
 * A filter that will only allow triggers for builds that complete in
 * certain states.
 */
public class BuildCompletedEventFilter implements EventTriggerFilter
{
    public static final String PARAM_PROJECT            = "other.project";
    public static final String PARAM_STATES             = "build.states";
    public static final String PARAM_PROPAGATE_REVISION = "propagate.revision";
    public static final String PARAM_REPLACEABLE        = "replaceable";
    public static final String SEPARATOR = ",";

    public boolean accept(Trigger trigger, Event event, TaskExecutionContext context)
    {
        BuildCompletedEvent bce = (BuildCompletedEvent) event;
        Map<Serializable, Serializable> dataMap = trigger.getDataMap();
        boolean accept = !bce.getBuildResult().isPersonal() && checkProject(dataMap, bce) && checkState(dataMap, bce);
        if (accept)
        {
            // Pass some information to the task.
            if (getBooleanParam(dataMap, PARAM_PROPAGATE_REVISION, false))
            {
                // Copy the revision: we don't want to shage the persistent instance.
                context.put(BuildProjectTask.PARAM_REVISION, new Revision(bce.getBuildResult().getRevision().getRevisionString()));
                context.put(BuildProjectTask.PARAM_REPLACEABLE, getBooleanParam(dataMap, PARAM_REPLACEABLE, false));
            }
        }
        return accept;
    }

    public boolean dependsOnProject(Trigger trigger, long projectId)
    {
        Long triggerProject = (Long) trigger.getDataMap().get(PARAM_PROJECT);
        return triggerProject != null && triggerProject == projectId;
    }

    private boolean checkProject(Map<Serializable, Serializable> dataMap, BuildCompletedEvent event)
    {
        Long projectId = (Long) dataMap.get(PARAM_PROJECT);
        return projectId == null || projectId == event.getBuildResult().getProject().getId();
    }

    private boolean checkState(Map<Serializable, Serializable> dataMap, BuildCompletedEvent event)
    {
        String stateString = (String) dataMap.get(PARAM_STATES);
        if(stateString == null || stateString.trim().length() == 0)
        {
            return true;
        }

        try
        {
            ResultState[] states = ResultState.getStates(stateString);
            if(states != null)
            {
                ResultState state = event.getBuildResult().getState();
                for(ResultState s: states)
                {
                    if(s == state)
                    {
                        return true;
                    }
                }
            }
        }
        catch (IllegalArgumentException e)
        {
            // Fall through to false
        }

        return false;
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

}
