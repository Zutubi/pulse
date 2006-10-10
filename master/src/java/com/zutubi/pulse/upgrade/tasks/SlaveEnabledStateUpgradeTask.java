package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.ResultSet;

/**
 */
public class SlaveEnabledStateUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Slave enabled state";
    }

    public String getDescription()
    {
        return "Introduces more enabled states for slave agents to allow for automatic upgrades.";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        updateStates(con);
        dropColumn(con);
    }

    private void updateStates(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, enabled FROM slave");
            rs = stmt.executeQuery();

            while (rs.next())
            {
                setEnabledState(con, JDBCUtils.getLong(rs, "id"), JDBCUtils.getBool(rs, "enabled"));
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private void setEnabledState(Connection con, Long id, Boolean enabled) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("UPDATE slave SET enable_state = ? WHERE id = ?");
            stmt.setString(1, enabled ? "ENABLED" : "DISABLED");
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void dropColumn(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("ALTER TABLE slave DROP COLUMN enabled");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }
}
