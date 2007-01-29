package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.model.persistence.hibernate.PersistenceTestCase;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.SQLException;

/**
 * <class-comment/>
 */
public class MigrateSchemaUpgradeTaskTest extends PersistenceTestCase
{

    public MigrateSchemaUpgradeTaskTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
    }

    protected String[] getConfigLocations()
    {
        return new String[]{"com/zutubi/pulse/bootstrap/testBootstrapContext.xml",
                "com/zutubi/pulse/upgrade/tasks/testSchemaUpgradeTaskContext.xml"};
    }

    public void testAddTableWithColumn() throws Exception
    {
        UpgradeTask upgrade;

        // before, test that table is not there.
        assertFalse(checkTableExists("TEST"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/upgrade/tasks/testSchemaMigration-v1.hbm.xml");
        upgrade.execute(new MockUpgradeContext());

        // after, tet that table is there.
        assertTrue(checkTableExists("TEST"));
        assertTrue(checkColumnExists("TEST", "NAME"));
    }

    public void testAddColumnToExistingTable() throws Exception
    {
        UpgradeTask upgrade;

        // before, test that table is not there.
        assertFalse(checkTableExists("TEST"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/upgrade/tasks/testSchemaMigration-v1.hbm.xml");
        upgrade.execute(new MockUpgradeContext());

        // after, tet that table is there.
        assertFalse(checkColumnExists("TEST", "NEW_COLUMN"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/upgrade/tasks/testSchemaMigration-v2.hbm.xml");
        upgrade.execute(new MockUpgradeContext());

        assertTrue(checkColumnExists("TEST", "NEW_COLUMN"));
    }

/*
    public void testFK() throws SQLException, UpgradeException, IOException
    {
        MutableConfiguration config = new MutableConfiguration();

        Properties props = console.getConfig().getHibernateProperties();
        props.put("hibernate.connection.provider_class", "com.zutubi.pulse.upgrade.tasks.HackyUpgradeTaskConnectionProvider");

        // slight hack to provide hibernate with access to the configured datasource.
        HackyUpgradeTaskConnectionProvider.dataSource = dataSource;

        String[] mappings = new String[]{"com/zutubi/pulse/upgrade/tasks/testSchemaMigration-v4.hbm.xml"};

        // use spring to help load the classpath resources. Rather useful actually.
        for (String mapping : mappings)
        {
            ClassPathResource resource = new ClassPathResource(mapping);
            config.addInputStream(resource.getInputStream());
        }

        // before, test that table is not there.
        assertFalse(checkTableExists("BASE"));

        SchemaUpdate schemaUpdate = new SchemaUpdate(config, props);
        schemaUpdate.execute(true, true);

        assertTrue(checkTableExists("BASE"));
        assertTrue(checkTableExists("SECONDARY_A"));
        assertTrue(checkTableExists("SECONDARY_B"));
        assertTrue(checkTableExists("SECONDARY_C"));

        SchemaRefactor refactor = new SchemaRefactor(config, props);
        refactor.renameTable("BASE", "BASE_RENAME");

        assertTrue(checkTableExists("BASE_RENAME"));

        // transfer data...

        assertFalse(checkTableExists("BASE"));
        assertTrue(checkTableExists("SECONDARY_A"));
        assertTrue(checkTableExists("SECONDARY_B"));
        assertTrue(checkTableExists("SECONDARY_C"));

        assertTrue(checkColumnExists("BASE_RENAME", "A"));
        assertFalse(checkColumnExists("BASE_RENAME", "B"));
        
        refactor.renameColumn("BASE_RENAME", "A", "B");

        assertFalse(checkColumnExists("BASE_RENAME", "A"));
        assertTrue(checkColumnExists("BASE_RENAME", "B"));
    }

    private void processSchemaModification(String ...mappings) throws IOException
    {
        Configuration config = new Configuration();

        Properties props = console.getConfig().getHibernateProperties();
        props.put("hibernate.connection.provider_class", "com.zutubi.pulse.upgrade.tasks.HackyUpgradeTaskConnectionProvider");

        // slight hack to provide hibernate with access to the configured datasource.
        HackyUpgradeTaskConnectionProvider.dataSource = dataSource;

        // use spring to help load the classpath resources. Rather useful actually.
        for (String mapping : mappings)
        {
            ClassPathResource resource = new ClassPathResource(mapping);
            config.addInputStream(resource.getInputStream());
        }

        // run the schema update.
        SchemaUpdate schemaUpdate = new SchemaUpdate(config, props);
        schemaUpdate.execute(true, true);

        List<Exception> exceptions = schemaUpdate.getExceptions();
        assertEquals(0, exceptions.size());
    }

    private void dropConstraints(String tableName, String ...mappings) throws IOException, SQLException
    {
        Configuration config = new Configuration();

        Properties props = console.getConfig().getHibernateProperties();
        props.put("hibernate.connection.provider_class", "com.zutubi.pulse.upgrade.tasks.HackyUpgradeTaskConnectionProvider");

        // slight hack to provide hibernate with access to the configured datasource.
        HackyUpgradeTaskConnectionProvider.dataSource = dataSource;

        // use spring to help load the classpath resources. Rather useful actually.
        for (String mapping : mappings)
        {
            ClassPathResource resource = new ClassPathResource(mapping);
            config.addInputStream(resource.getInputStream());
        }

        HibernateUtils.dropConstraints(config, dataSource, tableName);
    }
*/

    private UpgradeTask newSchemaUpgrade(String mapping)
    {
        MigrateSchemaUpgradeTask task = new MigrateSchemaUpgradeTask();
        task.setMapping(mapping);
        task.setDataSource(dataSource);
        task.setDatabaseConsole(console);
        return task;
    }

    private boolean checkTableExists(String tableName) throws SQLException
    {
        return JDBCUtils.tableExists(dataSource, tableName);
    }

    private boolean checkColumnExists(String tableName, String columnName) throws SQLException
    {
        return JDBCUtils.columnExists(dataSource, tableName, columnName);
    }
}