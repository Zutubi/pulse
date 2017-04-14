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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.database.DatabaseConsole;
import com.zutubi.pulse.master.database.DatabaseConsoleBeanFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.jdbcDriver;

import java.sql.DriverManager;
import java.util.List;

public abstract class BaseUpgradeTaskTestCase extends PulseTestCase
{
    protected BasicDataSource dataSource;
    protected DatabaseConfig databaseConfig;
    protected DatabaseConsole databaseConsole;

    protected void setUp() throws Exception
    {
        super.setUp();

        SpringComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/master/bootstrap/testBootstrapContext.xml");
        dataSource = SpringComponentContext.getBean("dataSource");
        databaseConfig = SpringComponentContext.getBean("databaseConfig");

        // initialise required schema.
        DatabaseConsoleBeanFactory factory = new DatabaseConsoleBeanFactory();
        factory.setDatabaseConfig((DatabaseConfig) SpringComponentContext.getBean("databaseConfig"));
        factory.setDataSource(dataSource);
        factory.setHibernateMappings(getMappings());

        DriverManager.registerDriver(new jdbcDriver());
        databaseConsole = (DatabaseConsole) factory.getObject();
        databaseConsole.createSchema();
    }

    protected void tearDown() throws Exception
    {
        JDBCUtils.execute(dataSource, "SHUTDOWN");
        dataSource.close();
        databaseConsole.stop(false);
        databaseConsole = null;
        databaseConfig = null;

        SpringComponentContext.closeAll();

        super.tearDown();
    }

    /**
     * The list of hibernate mappings that defines the original schema that we are upgrading
     * from.  The list must define locations on the classpath.
     *
     * @return list of mappings.
     */
    protected abstract List<String> getMappings();
}
