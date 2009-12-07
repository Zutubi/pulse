package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Update the existing dependency triggers, changing the trigger type from event to noop.
 * This is because the dependency triggering is now handled by the build scheduling system
 * rather than being triggered by a build completed event.  All new dependent build triggers
 * now use the noop trigger.
 */
public class DependentBuildTriggerUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement("update LOCAL_TRIGGER set FILTER_CLASS = ?, TRIGGER_TYPE = ?, TRIGGER_EVENT = ? where FILTER_CLASS = ?");

            JDBCUtils.setString(ps, 1, null);
            JDBCUtils.setString(ps, 2, "NOOP");
            JDBCUtils.setString(ps, 3, null);
            JDBCUtils.setString(ps, 4, "com.zutubi.pulse.master.DependentBuildEventFilter");

            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }
}

