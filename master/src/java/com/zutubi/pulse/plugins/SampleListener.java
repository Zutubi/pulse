package com.zutubi.pulse.plugins;

import com.zutubi.pulse.events.AllEventListener;
import com.zutubi.pulse.events.Event;

/**
 * <class-comment/>
 */
public class SampleListener extends AllEventListener
{
    public void handleEvent(Event evt)
    {
        System.err.println(evt);
    }
}
