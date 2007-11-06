package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * <class-comment/>
 */
public class RetainWorkingCopyUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Retain working copy";
    }

    public String getDescription()
    {
        return "This upgrade tasks sets the retain working copy option for all existing build specifications.";
    }

    public void execute(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("UPDATE build_specification SET retain_working_copy = true");
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
