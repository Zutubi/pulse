/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.events.Event;

/**
 */
public class PassFilter implements EventTriggerFilter
{
    public boolean accept(Trigger trigger, Event event)
    {
        return true;
    }
}
