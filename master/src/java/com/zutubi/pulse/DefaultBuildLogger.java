package com.zutubi.pulse;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.pulse.events.build.OutputEvent;

import java.io.File;

/**
 *
 *
 */
public class DefaultBuildLogger extends AbstractFileLogger implements BuildLogger, EventListener
{
    public DefaultBuildLogger(File logFile)
    {
        super(logFile);
    }

    public void prepare()
    {
        openWriter();
    }

    public void log(Event event)
    {
        if (event instanceof OutputEvent)
        {
            log((OutputEvent)event);
        }
        else
        {
            logMarker(event.toString());
        }
    }

    public void log(OutputEvent evt)
    {
        logMarker(new String(evt.getData()));
    }

    public void done()
    {
        closeWriter();
    }

    public void handleEvent(Event event)
    {
        log(event);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildEvent.class};
    }
}
