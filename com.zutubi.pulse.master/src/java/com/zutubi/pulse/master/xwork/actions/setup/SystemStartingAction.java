package com.zutubi.pulse.master.xwork.actions.setup;

import com.zutubi.pulse.master.xwork.actions.agents.ServerMessagesActionSupport;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Collections;
import java.util.List;

/**
 * Action for the auto-refreshing holding page that is displayed while Pulse
 * starts up.  If an error occurs during startup, this action will detect it
 * for display to the user.
 */
public class SystemStartingAction extends ServerMessagesActionSupport
{
    private static final int MAX_ERRORS = 5;

    private List<CustomLogRecord> errorRecords;

    public List<CustomLogRecord> getErrorRecords()
    {
        return errorRecords;
    }

    @Override
    public String execute() throws Exception
    {
        errorRecords = CollectionUtils.filter(serverMessagesHandler.takeSnapshot(), new Predicate<CustomLogRecord>()
        {
            public boolean satisfied(CustomLogRecord customLogRecord)
            {
                return isError(customLogRecord);
            }
        });

        Collections.reverse(errorRecords);
        if (errorRecords.size() > MAX_ERRORS)
        {
            errorRecords = errorRecords.subList(0, MAX_ERRORS);
        }

        return SUCCESS;
    }
}
