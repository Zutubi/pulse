package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Forces triggers in the NONE state to SCHEDULED.
 */
public class TriggerStateUpgradeTask extends DatabaseUpgradeTask
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
            update = con.prepareStatement("update LOCAL_TRIGGER set STATE = 'SCHEDULED' where STATE = 'NONE'");
            update.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(update);
        }
    }
}
