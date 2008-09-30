package com.zutubi.pulse.web;

import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.web.agents.ServerMessagesActionSupport;

import java.util.Collections;
import java.util.List;

/**
 * Looks up recent errors to show on the internal error page.
 */
public class InternalErrorAction extends ServerMessagesActionSupport
{
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
}
