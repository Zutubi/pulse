package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <class-comment/>
 */
public class RepairUniqueKeyUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Repair unique key";
    }

    public String getDescription()
    {
        return "Repairs the unique key table which may have been damaged in a beta upgrade.";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT next_hi FROM hibernate_unique_key", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            rs.last();
            if(rs.getRow() > 1)
            {
                // Repairing to do, find the highest id
                long highest = 0;
                do
                {
                    long value = rs.getLong(1);
                    if(value > highest)
                    {
                        highest = value;
                    }
                }
                while(rs.previous());

                updateKey(con, highest);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void updateKey(Connection con, long highest) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("DELETE FROM hibernate_unique_key WHERE next_hi != ?");
            JDBCUtils.setLong(stmt, 1, highest);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
