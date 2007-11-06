package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <class-comment/>
 */
public class ScmMonitorDataUpgradeTask extends DatabaseUpgradeTask
{
    /**
     * @see com.zutubi.pulse.upgrade.UpgradeTask#getName()
     */
    public String getName()
    {
        return "Scm monitor field initialisation.";
    }

    /**
     * @see com.zutubi.pulse.upgrade.UpgradeTask#getDescription()
     */
    public String getDescription()
    {
        return "This upgrade task initialises the new scm monitor field, setting its value to true.";
    }

    /**
     * @see com.zutubi.pulse.upgrade.UpgradeTask#execute()
     */
    public void execute(Connection con) throws SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement("UPDATE scm SET monitor = ?");
            JDBCUtils.setBool(ps, 1, true);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    /**
     * Failure in this upgrade is non-fatal, just annoying.
     *
     * @return false
     */
    public boolean haltOnFailure()
    {
        return false;
    }
}
