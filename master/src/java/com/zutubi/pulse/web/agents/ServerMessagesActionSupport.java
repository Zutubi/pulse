package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.web.ActionSupport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

/**
 * Helper base class for actions that display server messages.
 */
public class ServerMessagesActionSupport extends AgentActionSupport
{
    public boolean isError(CustomLogRecord record)
    {
        return record.getLevel() == Level.SEVERE;
    }

    public boolean isWarning(CustomLogRecord record)
    {
        return record.getLevel() == Level.WARNING;
    }

    public boolean hasThrowable(CustomLogRecord record)
    {
        return record.getThrown() != null;
    }

    public String getStackTrace(CustomLogRecord record)
    {
        Throwable t = record.getThrown();
        if (t != null)
        {
            StringWriter writer = new StringWriter();
            t.printStackTrace(new PrintWriter(writer));
            return writer.getBuffer().toString();
        }
        else
        {
            return "";
        }
    }

}
