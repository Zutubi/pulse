package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/**
 */
public class ResourceEnvironmentUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Resource environment";
    }

    public String getDescription()
    {
        return "Upgrade to resources to import values into the environment";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        addPropertyColumns("resource_properties", con);
        addPropertyColumns("resource_version_properties", con);
        updateProperties("resource_properties", con);
        updateProperties("resource_version_properties", con);
    }

    private void addPropertyColumns(String table, Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("ALTER TABLE " + table + " ADD COLUMN add_to_environment BOOLEAN DEFAULT false");
            stmt.executeUpdate();
            JDBCUtils.close(stmt);
            stmt = con.prepareCall("ALTER TABLE " + table + " ADD COLUMN add_to_path BOOLEAN DEFAULT false");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void updateProperties(String table, Connection con) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("UPDATE " + table + " SET name = 'JAVA_HOME', add_to_environment = true where name = 'java.home'");
            stmt.executeUpdate();
            JDBCUtils.close(stmt);
            stmt = con.prepareCall("UPDATE " + table + " SET name = 'ANT_HOME', add_to_environment = true where name = 'ant.home'");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        // No going on if the schema isn't updated
        return true;
    }
}
