/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
