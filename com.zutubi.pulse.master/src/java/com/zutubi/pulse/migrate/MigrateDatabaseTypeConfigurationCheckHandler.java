package com.zutubi.pulse.migrate;

import com.opensymphony.util.TextUtils;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.tove.config.setup.DatabaseType;
import com.zutubi.pulse.master.util.jdbc.DriverWrapper;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Checks the database details provided by the user by establishing a basic
 * JDBC connection.
 */
@SymbolicName("zutubi.setupMigrateDatabaseTypeConfigurationCheckHandler")
public class MigrateDatabaseTypeConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<MigrateDatabaseTypeConfiguration>
{
    private static final Logger LOG = Logger.getLogger(MigrateDatabaseTypeConfigurationCheckHandler.class);
    private static final String MYSQL_INSANITY = "** BEGIN NESTED EXCEPTION **";

    public void test(MigrateDatabaseTypeConfiguration configuration) throws Exception
    {
        DatabaseType type = configuration.getType();
        if (!type.isEmbedded())
        {
            // check the driver file if it is specified.
            if (TextUtils.stringSet(configuration.getDriverFile()))
            {
                File driverFile = new File(configuration.getDriverFile());
                URL driverUrl = driverFile.toURI().toURL();

                try
                {
                    URLClassLoader loader = new URLClassLoader(new URL[]{driverUrl});
                    Class driverClass = loader.loadClass(type.getJDBCClassName(configuration));
                    Driver driver = (Driver) driverClass.newInstance();
                    DriverWrapper shim = new DriverWrapper(driver);

                    try
                    {
                        DriverManager.registerDriver(shim);
                        
                        checkConnection(configuration);
                    }
                    finally
                    {
                        DriverManager.deregisterDriver(shim);
                    }
                }
                catch (Exception e)
                {
                    LOG.warning(e);
                    String message = e.getMessage();
                    if (e instanceof ClassNotFoundException)
                    {
                        message = "Unable to locate the requested driver " + e.getMessage();
                    }
                    int i = message.indexOf(MYSQL_INSANITY);
                    if(i >= 0)
                    {
                        message = message.substring(0, i).trim();
                    }

                    throw new PulseException(message, e);
                }
            }
            else
            {
                try
                {
                    checkConnection(configuration);
                }
                catch (Exception e)
                {
                    LOG.warning(e);
                    String message = e.getMessage();
                    int i = message.indexOf(MYSQL_INSANITY);
                    if(i >= 0)
                    {
                        message = message.substring(0, i).trim();
                    }

                    throw new PulseException(message, e);
                }
            }
        }
    }

    private void checkConnection(MigrateDatabaseTypeConfiguration configuration) throws SQLException
    {
        Connection connection = null;
        try
        {
            DatabaseType type = configuration.getType();

            String url = type.getJDBCUrl(configuration);
            String usr = configuration.getUser();
            String password = configuration.getPassword();
            
            connection = DriverManager.getConnection(url, usr, password);
            connection.getMetaData();
        }
        finally
        {
            JDBCUtils.close(connection);
        }
    }

}
