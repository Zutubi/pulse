package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.database.DatabaseConsole;
import com.zutubi.pulse.database.DatabaseConsoleBeanFactory;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.JDBCUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.jdbcDriver;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.DriverManager;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public abstract class BaseUpgradeTaskTestCase extends PulseTestCase
{
    protected BasicDataSource dataSource;
    protected DatabaseConfig databaseConfig;
    private DatabaseConsole databaseConsole;

    public BaseUpgradeTaskTestCase()
    {
    }

    public BaseUpgradeTaskTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        ComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/testBootstrapContext.xml");
        dataSource = (BasicDataSource) ComponentContext.getBean("dataSource");
        databaseConfig = (DatabaseConfig) ComponentContext.getBean("databaseConfig");

        // initialise required schema.
        DatabaseConsoleBeanFactory factory = new DatabaseConsoleBeanFactory();
        factory.setDatabaseConfig((DatabaseConfig) ComponentContext.getBean("databaseConfig"));
        factory.setDataSource(dataSource);
        factory.setHibernateMappings(getTestMappings());

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

        ComponentContext.closeAll();

        super.tearDown();
    }

    protected abstract List<String> getTestMappings();

    protected List<String> getMappings(String build)
    {
        List<String> mappings = new LinkedList<String>();

        String path = "master/src/test/com/zutubi/pulse/upgrade/schema/build_" + build;
        File mappingDir = new File(getPulseRoot(), path);
        for (File f : mappingDir.listFiles(new XMLFilenameFilter()))
        {
            mappings.add("com/zutubi/pulse/upgrade/schema/build_" + build + "/" + f.getName());
        }
        
        return mappings;
    }

    public class XMLFilenameFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".xml");
        }
    }


}
