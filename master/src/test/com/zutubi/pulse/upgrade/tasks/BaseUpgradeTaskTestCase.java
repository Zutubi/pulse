package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.bootstrap.DatabaseConsole;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.DatabaseConsoleBeanFactory;
import com.zutubi.pulse.bootstrap.DatabaseConfig;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.LinkedList;

import org.apache.commons.dbcp.BasicDataSource;

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

        databaseConsole = (DatabaseConsole) factory.getObject();
        databaseConsole.createSchema();
    }

    protected void tearDown() throws Exception
    {
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
