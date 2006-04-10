package com.zutubi.pulse.scm.cvs;

import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.event.EventManager;
import org.netbeans.lib.cvsclient.event.MessageEvent;

/**
 * <class-comment/>
 */
public class VersionBuilder implements Builder
{
    private EventManager eventManager;
    private VersionCommand command;

    private String versionStr;

    public VersionBuilder(EventManager eventMan, VersionCommand versionCommand)
    {
        this.eventManager = eventMan;
        this.command = versionCommand;
    }

    public void parseLine(String line, boolean isErrorMessage)
    {
        // we only expect a single line response here...
        if (!isErrorMessage)
        {
            versionStr = line;
        }
    }

    public void parseEnhancedMessage(String key, Object value)
    {
    }

    public void outputDone()
    {
        if (versionStr != null)
        {
            eventManager.fireCVSEvent(new MessageEvent(this, versionStr, false));
        }
    }
}
