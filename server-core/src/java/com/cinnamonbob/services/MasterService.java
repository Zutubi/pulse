package com.cinnamonbob.services;

import com.cinnamonbob.events.Event;

/**
 */
public interface MasterService
{
    void handleEvent(Event event);
}
