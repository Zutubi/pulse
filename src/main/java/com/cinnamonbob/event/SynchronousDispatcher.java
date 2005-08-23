package com.cinnamonbob.event;

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
