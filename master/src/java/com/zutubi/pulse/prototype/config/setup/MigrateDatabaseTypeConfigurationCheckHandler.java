package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.util.DriverWrapper;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Connection;

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
            File driverFile = new File(configuration.getDriverFile());
            URL driverUrl = driverFile.toURI().toURL();

            try
            {
                URLClassLoader loader = new URLClassLoader(new URL[]{driverUrl});
                Class driverClass = loader.loadClass(type.getJDBCClassName(configuration));
                Driver driver = (Driver) driverClass.newInstance();
                DriverWrapper shim = new DriverWrapper(driver);

                DriverManager.registerDriver(shim);
                Connection connection = null;
                try
                {
                    connection = DriverManager.getConnection(type.getJDBCUrl(configuration), configuration.getUser(), configuration.getPassword());
                    connection.getMetaData();
                }
                finally
                {
                    DriverManager.deregisterDriver(shim);
                    JDBCUtils.close(connection);
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
    }

}
