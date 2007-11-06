package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * <class-comment/>
 */
public class DropBuildChangelistsUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Drop build changelists";
    }

    public String getDescription()
    {
        return "This upgrade tasks removes links from builds to changelists that are no longer required.";
    }

    public void execute(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("UPDATE changelist SET build_scm_details_id = null");
            stmt.executeUpdate();
            JDBCUtils.close(stmt);
            stmt = con.prepareCall("ALTER TABLE changelist DROP COLUMN ordinal");
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
