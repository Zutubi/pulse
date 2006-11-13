package com.zutubi.pulse.plugins.descriptors;

import com.zutubi.pulse.events.AllEventListener;
import com.zutubi.pulse.events.Event;

/**
 * <class-comment/>
 */
public class MockListener extends AllEventListener
{
    public static int handleEventCount = 0;

    public void handleEvent(Event evt)
    {
        handleEventCount++;
    }
}
