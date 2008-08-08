package com.zutubi.pulse;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.pulse.events.build.OutputEvent;
import com.zutubi.pulse.events.build.BuildOutputCommencedEvent;
import com.zutubi.pulse.events.build.BuildOutputCompletedEvent;

import java.io.File;

/**
 *
 *
 */
public class DefaultBuildLogger extends AbstractFileLogger implements BuildLogger, EventListener
{
    private static final String PRE_MARKER = "============================[ task output below ]============================";
    private static final String POST_MARKER = "============================[ task output above ]============================";

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
            log((OutputEvent) event);
        }
        else if (event instanceof BuildOutputCommencedEvent)
        {
            if (writer != null)
            {
                writer.println(PRE_MARKER);
                writer.flush();
            }
        }
        else if (event instanceof BuildOutputCompletedEvent)
        {
            if (writer != null)
            {
                writer.println(POST_MARKER);
                writer.flush();
            }
        }
        else
        {
            logMarker(event.toString());
        }
    }

    public void log(OutputEvent evt)
    {
        if (writer != null)
        {
            writer.print(new String(evt.getData()));
            writer.flush();
        }
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
