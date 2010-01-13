package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.upgrade.UpgradeTask;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.io.File;

public class MigrateSchemaUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    protected List<String> getMappings()
    {
        // we are handling our own map processing.. 
        return new LinkedList<String>();
    }

    protected List<File> getTestFileMappings()
    {
        return new LinkedList<File>();
    }

    public void testAddTableWithColumn() throws Exception
    {
        UpgradeTask upgrade;

        // before, test that table is not there.
        assertFalse(checkTableExists("TEST"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/master/upgrade/tasks/testSchemaMigration-v1.hbm.xml");
        upgrade.execute();

        // after, tet that table is there.
        assertTrue(checkTableExists("TEST"));
        assertTrue(checkColumnExists("TEST", "NAME"));
    }

    public void testAddColumnToExistingTable() throws Exception
    {
        UpgradeTask upgrade;

        // before, test that table is not there.
        assertFalse(checkTableExists("TEST"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/master/upgrade/tasks/testSchemaMigration-v1.hbm.xml");
        upgrade.execute();

        // after, tet that table is there.
        assertFalse(checkColumnExists("TEST", "NEW_COLUMN"));

        upgrade = newSchemaUpgrade("com/zutubi/pulse/master/upgrade/tasks/testSchemaMigration-v2.hbm.xml");
        upgrade.execute();

        assertTrue(checkColumnExists("TEST", "NEW_COLUMN"));
    }

    private UpgradeTask newSchemaUpgrade(String mapping)
    {
        MigrateSchemaUpgradeTask task = new MigrateSchemaUpgradeTask();
        task.setMapping(mapping);
        task.setDataSource(dataSource);
        task.setDatabaseConfig(databaseConfig);
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