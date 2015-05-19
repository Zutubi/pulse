package com.zutubi.pulse.master.xwork.actions.setup;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.bootstrap.SetupState;
import com.zutubi.pulse.master.xwork.actions.agents.ServerMessagesActionSupport;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.tove.security.AccessManager;

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
    private SetupManager setupManager;

    public List<CustomLogRecord> getErrorRecords()
    {
        return errorRecords;
    }

    @Override
    public String execute() throws Exception
    {
        if (setupManager.getCurrentState() != SetupState.STARTING || accessManager.hasPermission(AccessManager.ACTION_ADMINISTER, null))
        {
            errorRecords = Lists.newLinkedList(Iterables.filter(serverMessagesHandler.takeSnapshot(), new Predicate<CustomLogRecord>()
            {
                public boolean apply(CustomLogRecord customLogRecord)
                {
                    return isError(customLogRecord);
                }
            }));

            Collections.reverse(errorRecords);
            if (errorRecords.size() > MAX_ERRORS)
            {
                errorRecords = errorRecords.subList(0, MAX_ERRORS);
            }
        }

        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
