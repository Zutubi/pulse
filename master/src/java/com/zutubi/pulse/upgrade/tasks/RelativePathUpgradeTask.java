package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.util.JDBCUtils;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 */
public class RelativePathUpgradeTask extends DatabaseUpgradeTask implements ConfigurationAware
{
    private MasterConfigurationManager configurationManager;

    public String getName()
    {
        return "Relative paths";
    }

    public String getDescription()
    {
        return "Upgrade to change absolute paths into paths relative to data root";
    }

    public void execute(Connection con) throws SQLException
    {
        // this is part of the initialisation process of the data directory. However, if the data directory
        // was created before this initialisation was introduced, the upgrade task is required.
        Data data = configurationManager.getData();

        updateTable(con, "build_result", data.getData().getAbsolutePath());
        updateTable(con, "recipe_result", data.getData().getAbsolutePath());
        updateTable(con, "command_result", data.getData().getAbsolutePath());
    }

    private void updateTable(Connection con, String table, String dataPath) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, outputdir FROM " + table);
            rs = stmt.executeQuery();

            while (rs.next())
            {
                relativisePath(con, table, JDBCUtils.getLong(rs, "id"), JDBCUtils.getString(rs, "outputdir"), dataPath);
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private void relativisePath(Connection con, String table, Long id, String path, String dataPath) throws SQLException
    {
        if(path != null && path.startsWith(dataPath))
        {
            path = path.substring(dataPath.length());
            if(path.startsWith(File.separator) || path.startsWith("/"))
            {
                path = path.substring(1);
            }

            CallableStatement stmt = null;
            try
            {
                stmt = con.prepareCall("UPDATE " + table + " SET outputdir = ? WHERE id = ?");
                stmt.setString(1, path);
                stmt.setLong(2, id);
                stmt.executeUpdate();
            }
            finally
            {
                JDBCUtils.close(stmt);
            }
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
