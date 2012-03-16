package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Changes the status of SUCCESS results with warnings to the new WARNINGS status.
 */
public class WarningStatusUpgradeTask extends DatabaseUpgradeTask
{
    @Override
    public void execute(Connection con) throws Exception
    {
        updateTable(con, "BUILD_RESULT", "STATE");
        updateTable(con, "RECIPE_RESULT", "STATE");
        updateTable(con, "COMMAND_RESULT", "stateName");
    }

    private void updateTable(Connection con, String table, String column) throws SQLException
    {
        PreparedStatement statement = con.prepareStatement("UPDATE " + table + " SET " + column + " = 'WARNINGS' WHERE " + column + " = 'SUCCESS' AND WARNING_FEATURE_COUNT > 0");
        try
        {
            statement.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(statement);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
