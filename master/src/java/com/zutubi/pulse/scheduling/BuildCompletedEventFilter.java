package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.util.logging.Logger;

/**
 * A filter that will only allow triggers for builds that complete in
 * certain states.
 */
public class BuildCompletedEventFilter implements EventTriggerFilter
{
    private static final Logger LOG = Logger.getLogger(BuildCompletedEventFilter.class);

    public static final String PARAM_PROJECT = "other.project";
    public static final String PARAM_SPECIFICATION = "other.spec";
    public static final String PARAM_STATES = "build.states";
    public static final String SEPARATOR = ",";

    public static String getStatesString(ResultState... states)
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for(ResultState s: states)
        {
            if(first)
            {
                first = false;
            }
            else
            {
                result.append(SEPARATOR);
            }

            result.append(s.toString());
        }

        return result.toString();
    }

    public static ResultState[] getStates(String value)
    {
        try
        {
            String[] parts = value.split(SEPARATOR);
            ResultState[] states = new ResultState[parts.length];

            for(int i = 0; i < parts.length; i++)
            {
                states[i] = ResultState.valueOf(parts[i]);
            }

            return states;
        }
        catch (IllegalArgumentException e)
        {
            LOG.severe(e);
            return null;
        }
    }

    public boolean accept(Trigger trigger, Event event)
    {
        BuildCompletedEvent bce = (BuildCompletedEvent) event;
        return checkProject(trigger, bce) && checkSpec(trigger, bce) && checkState(trigger, bce);
    }

    private boolean checkProject(Trigger trigger, BuildCompletedEvent event)
    {
        Long projectId = (Long) trigger.getDataMap().get(PARAM_PROJECT);
        return projectId == null || projectId == event.getResult().getProject().getId();
    }

    private boolean checkSpec(Trigger trigger, BuildCompletedEvent event)
    {
        String spec = (String) trigger.getDataMap().get(PARAM_SPECIFICATION);
        return spec == null || spec.equals(event.getResult().getBuildSpecification());
    }

    private boolean checkState(Trigger trigger, BuildCompletedEvent event)
    {
        String stateString = (String) trigger.getDataMap().get(PARAM_STATES);
        if(stateString == null || stateString.trim().length() == 0)
        {
            return true;
        }

        ResultState[] states = getStates(stateString);
        if(states != null)
        {
            ResultState state = event.getResult().getState();
            for(ResultState s: states)
            {
                if(s == state)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
