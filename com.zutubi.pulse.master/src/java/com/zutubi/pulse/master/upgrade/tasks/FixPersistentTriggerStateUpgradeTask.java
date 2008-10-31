package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.IOException;

/**
 * This upgrade tasks fixes a bug with the persistent state of upgrade tasks. CIB-1670
 * <p/>
 * The problem is that scheduled triggers have the persistent state of NONE.  To fix,
 * go through and update all triggers with NONE (which should not be the case) and set
 * them to SCHEDULED.
 */
public class FixPersistentTriggerStateUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Fix persistent trigger state.";
    }

    public String getDescription()
    {
        return "Update the persistent trigger state from NONE to SCHEDULED.";
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(Connection con) throws IOException, SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement("UPDATE local_trigger SET state = 'SCHEDULED' where state = 'NONE'");
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }
}
