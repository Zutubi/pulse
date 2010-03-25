package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Sets the explicit flag to false for existing implicit artifacts.
 */
public class ExplicitArtifactsUpgradeTask extends DatabaseUpgradeTask
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
            update = con.prepareStatement("update ARTIFACT set EXPLICIT = false where NAME in ('bootstrap output', 'command output', 'environment', 'retrieve output')");
            update.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(update);
        }
    }
}