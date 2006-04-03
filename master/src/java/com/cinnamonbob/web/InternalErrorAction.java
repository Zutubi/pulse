package com.cinnamonbob.web;

import com.cinnamonbob.logging.CustomLogRecord;
import com.cinnamonbob.logging.ServerMessagesHandler;

import java.util.Collections;
import java.util.List;

/**
 * Looks up recent errors to show on the internal error page.
 */
public class InternalErrorAction extends ServerMessagesActionSupport
{
    private ServerMessagesHandler serverMessagesHandler;
    private List<CustomLogRecord> records;

    public List<CustomLogRecord> getRecords()
    {
        return records;
    }

    public String execute() throws Exception
    {
        records = serverMessagesHandler.takeSnapshot();
        Collections.reverse(records);
        if (records.size() > 4)
        {
            records = records.subList(0, 4);
        }

        return SUCCESS;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }
}
