package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 */
public class BuildNumberUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Build number";
    }

    public String getDescription()
    {
        return "Upgrade to store next build number with project";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id FROM project");
            rs = stmt.executeQuery();

            while (rs.next())
            {
                setBuildNumber(con, JDBCUtils.getLong(rs, "id"));
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private void setBuildNumber(Connection con, Long id) throws SQLException
    {
        long number = getHighestBuildNumber(con, id);

        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("UPDATE project SET next_build_number = ? WHERE id = ?");
            stmt.setLong(1, number);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private long getHighestBuildNumber(Connection con, Long id) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT number FROM build_result WHERE project = ? ORDER BY number DESC LIMIT 1");
            stmt.setLong(1, id);
            rs = stmt.executeQuery();

            if(rs.next())
            {
                return JDBCUtils.getLong(rs, "number") + 1;
            }
            else
            {
                return 1;
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
