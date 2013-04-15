package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.GenericOutputEvent;
import com.zutubi.util.logging.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows the connection of output event streams from agents with {@link OutputStream}s on the
 * master.
 */
public class SlaveOutputListener implements EventListener
{
    private static final Logger LOG = Logger.getLogger(SlaveOutputListener.class);

    private long nextId = 1;
    private Map<Long, OutputStream> idToStream = new HashMap<Long, OutputStream>();

    public synchronized long registerStream(OutputStream stream)
    {
        long id = nextId++;
        idToStream.put(id, stream);
        return id;
    }

    public synchronized OutputStream unregisterStream(long id)
    {
        return idToStream.remove(id);
    }

    public void handleEvent(Event event)
    {
        GenericOutputEvent gev = (GenericOutputEvent) event;
        OutputStream stream;
        synchronized (this)
        {
            stream = idToStream.get(gev.getStreamId());
        }

        if (stream != null)
        {
            try
            {
                stream.write(gev.getData());
            }
            catch (IOException e)
            {
                LOG.warning("Could not write output event to local stream: " + e.getMessage(), e);
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ GenericOutputEvent.class };
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
