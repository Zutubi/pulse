package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.config.FileConfig;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.util.JDBCUtils;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <class-comment/>
 */
public class AdminUserUpgradeTask extends DatabaseUpgradeTask implements ConfigurationAware
{
    private MasterConfigurationManager configurationManager;

    public String getName()
    {
        return "Admin user";
    }

    public String getDescription()
    {
        return "Marks one user as the special admin user";
    }

    public void execute(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT login FROM user ORDER BY id ASC LIMIT 1");
            rs = stmt.executeQuery();
            if (rs.next())
            {
                String login = rs.getString("login");
                ConfigSupport appConfig = new ConfigSupport(new FileConfig(new File(configurationManager.getUserPaths().getUserConfigRoot(), "pulse.properties")));
                appConfig.setProperty("admin.login", login);
            }
            else
            {
                errors.add("Unable to find any user to mark as admin!");
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
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
