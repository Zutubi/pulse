package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.IOException;

/**
 * Update the result state string in the database to keep it in line with the
 * rename of the INITIAL state to PENDING
 */
public class RenameInitialResultStateToPendingUpgradeTask  extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        executeUpdate(con, "update BUILD_RESULT set STATE = 'PENDING' where STATE = 'INITIAL'");
        executeUpdate(con, "update RECIPE_RESULT set STATE = 'PENDING' where STATE = 'INITIAL'");
        executeUpdate(con, "update COMMAND_RESULT set stateName = 'PENDING' where stateName = 'INITIAL'");
    }

    private void executeUpdate(Connection con, String sql) throws SQLException
    {
        PreparedStatement statement = null;
        try
        {
            statement = con.prepareStatement(sql);
            statement.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(statement);
        }
    }
}
