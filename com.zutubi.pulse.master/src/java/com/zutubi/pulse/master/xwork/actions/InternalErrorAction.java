package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.master.xwork.actions.agents.ServerMessagesActionSupport;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.tove.security.AccessManager;

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
        if (accessManager.hasPermission(AccessManager.ACTION_ADMINISTER, null))
        {
            records = serverMessagesHandler.takeSnapshot();
            Collections.reverse(records);
            if (records.size() > 4)
            {
                records = records.subList(0, 4);
            }
        }

        return SUCCESS;
    }
}
