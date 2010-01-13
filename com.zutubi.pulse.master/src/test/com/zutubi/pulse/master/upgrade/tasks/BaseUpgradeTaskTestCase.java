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
        dataSource = (BasicDataSource) SpringComponentContext.getBean("dataSource");
        databaseConfig = (DatabaseConfig) SpringComponentContext.getBean("databaseConfig");

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
