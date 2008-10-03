package com.zutubi.pulse.master.web.agents;

import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.pulse.servercore.util.logging.ServerMessagesHandler;

import java.util.logging.Level;

/**
 * Helper base class for actions that display server messages.
 */
public class ServerMessagesActionSupport extends AgentActionBase
{
    protected ServerMessagesHandler serverMessagesHandler;

    public boolean isError(CustomLogRecord record)
    {
        return record.getLevel().intValue() == Level.SEVERE.intValue();
    }

    public boolean isWarning(CustomLogRecord record)
    {
        return record.getLevel().intValue() == Level.WARNING.intValue();
    }

    public boolean hasThrowable(CustomLogRecord record)
    {
        return record.getStackTrace().length() > 0;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }
}
