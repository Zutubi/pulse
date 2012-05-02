package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.tove.config.AbstractDatabaseConfigurationCheckHandler;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.reflection.DelegatingInvocationHandler;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

/**
 * Checks the database details provided by the user by establishing a basic
 * JDBC connection.
 */
@SymbolicName("zutubi.setupDatabaseTypeConfigurationCheckHandler")
public class SetupDatabaseTypeConfigurationCheckHandler extends AbstractDatabaseConfigurationCheckHandler<SetupDatabaseTypeConfiguration>
{
    public void test(SetupDatabaseTypeConfiguration configuration) throws Exception
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
                Driver shim = DelegatingInvocationHandler.newProxy(Driver.class, driver);

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
                processException(e);
            }
        }
    }

}
