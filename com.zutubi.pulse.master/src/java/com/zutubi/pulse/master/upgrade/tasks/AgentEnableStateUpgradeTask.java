package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Sets agents in the UPGRADING and FAILED_UPGRADE states to DISABLED as hosts
 * now hold persistent upgrade information.
 */
public class AgentEnableStateUpgradeTask extends DatabaseUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement update = null;
        try
        {
            update = con.prepareStatement("update AGENT_STATE set ENABLE_STATE = 'DISABLED' where ENABLE_STATE = 'FAILED_UPGRADE' or ENABLE_STATE = 'UPGRADING'");
            update.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(update);
        }
    }
}
