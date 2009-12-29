package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Deletes the agent statistics trigger that was missed by
 * {@link DeleteOldCallbackTriggersUpgradeTask} because it had its name and
 * group muddled.
 */
public class DeleteStatisticsCallbackTriggerUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(Connection con) throws Exception
    {
        PreparedStatement delete = null;
        try
        {
            delete = con.prepareStatement("DELETE FROM LOCAL_TRIGGER WHERE TRIGGER_NAME = 'services' AND TRIGGER_GROUP = 'statistics'");
            delete.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(delete);
        }
    }
}