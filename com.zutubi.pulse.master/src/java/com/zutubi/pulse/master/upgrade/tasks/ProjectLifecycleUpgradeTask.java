package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This upgrade task puts all projects into the initial state to make sure they
 * fit in with new project lifecycle changes.
 */
public class ProjectLifecycleUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement statement = null;
        try
        {
            // This takes the chance that no Git project (ones requiring init
            // at this point) is paused.  There is a workaround of manual
            // re-init.  It seems better than unpausing everyone's paused
            // projects unnecessarily.
            statement = con.prepareStatement("update PROJECT set STATE = 'INITIAL' where STATE != 'PAUSED'");
            statement.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(statement);
        }
    }
}
