/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.services;

import com.zutubi.pulse.events.Event;

/**
 */
public interface MasterService
{
    void handleEvent(Event event);
}
