package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 */
public class EstimatedRunningTimeUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Estimated running time";
    }

    public String getDescription()
    {
        return "Upgrade to result time stamps to support estimated running time";
    }

    public void execute(Connection con) throws SQLException
    {
        addColumn("build_result", con);
        addColumn("recipe_result", con);
        addColumn("command_result", con);
    }

    private void addColumn(String table, Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("ALTER TABLE " + table + " ADD COLUMN estimated_running_time BIGINT DEFAULT -1");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        // No going on if the schema isn't updated
        return true;
    }
}
