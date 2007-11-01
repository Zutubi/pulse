package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.build.BuildCompletedEvent;

/**
 * A filter that will only allow triggers for builds that complete in
 * certain states.
 */
public class BuildCompletedEventFilter implements EventTriggerFilter
{
    public static final String PARAM_PROJECT = "other.project";
    public static final String PARAM_STATES = "build.states";
    public static final String SEPARATOR = ",";

    public boolean accept(Trigger trigger, Event event)
    {
        BuildCompletedEvent bce = (BuildCompletedEvent) event;
        return !bce.getBuildResult().isPersonal() && checkProject(trigger, bce) && checkState(trigger, bce);
    }

    public boolean dependsOnProject(Trigger trigger, long projectId)
    {
        Long triggerProject = (Long) trigger.getDataMap().get(PARAM_PROJECT);
        return triggerProject != null && triggerProject == projectId;
    }

    private boolean checkProject(Trigger trigger, BuildCompletedEvent event)
    {
        Long projectId = (Long) trigger.getDataMap().get(PARAM_PROJECT);
        return projectId == null || projectId == event.getBuildResult().getProject().getId();
    }

    private boolean checkState(Trigger trigger, BuildCompletedEvent event)
    {
        String stateString = (String) trigger.getDataMap().get(PARAM_STATES);
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

}
