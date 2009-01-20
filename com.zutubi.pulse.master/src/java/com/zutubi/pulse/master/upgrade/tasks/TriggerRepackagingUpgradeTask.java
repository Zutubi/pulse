package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;

/**
 * This upgrade task renames the packages for classes reference by the trigger
 * table.
 */
public class TriggerRepackagingUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement query = null;
        PreparedStatement update = null;
        ResultSet rs = null;
        try
        {
            // This takes the chance that no Git project (ones requiring init
            // at this point) is paused.  There is a workaround of manual
            // re-init.  It seems better than unpausing everyone's paused
            // projects unnecessarily.
            query = con.prepareStatement("select ID, TASK_CLASS, TRIGGER_EVENT, FILTER_CLASS from LOCAL_TRIGGER");
            rs = query.executeQuery();
            update = con.prepareStatement("update LOCAL_TRIGGER set TASK_CLASS = ?, TRIGGER_EVENT = ?, FILTER_CLASS = ? where ID = ?");
            while (rs.next())
            {
                updateRow(rs, update);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(update);
            JDBCUtils.close(query);
        }
    }

    private void updateRow(ResultSet rs, PreparedStatement update) throws SQLException
    {
        for (int i = 1; i <= 3; i++)
        {
            String current = rs.getString(i + 1);
            if (current != null)
            {
                current = current.replace(".pulse", ".pulse.master");
            }
            update.setString(i, current);
        }

        update.setLong(4, rs.getLong(1));
        update.executeUpdate();
    }
}
