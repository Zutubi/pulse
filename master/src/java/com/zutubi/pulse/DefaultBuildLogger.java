package com.zutubi.pulse;

import com.zutubi.pulse.events.build.BuildEvent;

import java.io.File;

/**
 *
 *
 */
public class DefaultBuildLogger extends AbstractFileLogger implements BuildLogger
{
    public DefaultBuildLogger(File logFile)
    {
        super(logFile);
    }

    public void prepare()
    {
        openWriter();
    }

    public void log(BuildEvent event)
    {
        logMarker(event.toString());
    }

    public void done()
    {
        closeWriter();
    }
}
