package com.cinnamonbob.scheduling;

import com.cinnamonbob.events.Event;

/**
 */
public class BlockFilter implements EventTriggerFilter
{
    public boolean accept(Trigger trigger, Event event)
    {
        return false;
    }
}
