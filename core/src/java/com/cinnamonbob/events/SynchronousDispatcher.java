package com.cinnamonbob.events;

import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventDispatcher;
import com.cinnamonbob.events.EventListener;

import java.util.List;

/**
 * <class-comment/>
 */
public class SynchronousDispatcher implements EventDispatcher
{
    public void dispatch(Event evt, List<EventListener> listeners)
    {
        for (EventListener listener : listeners)
        {
            listener.handleEvent(evt);
        }
    }
}
