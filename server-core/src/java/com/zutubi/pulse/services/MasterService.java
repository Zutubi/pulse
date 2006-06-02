package com.zutubi.pulse.services;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.core.model.Resource;

import java.util.List;

/**
 */
public interface MasterService
{
    void handleEvent(Event event);

    Resource getResource(long slaveId, String name);
    List<String> getResourceNames(long slaveId);
}
