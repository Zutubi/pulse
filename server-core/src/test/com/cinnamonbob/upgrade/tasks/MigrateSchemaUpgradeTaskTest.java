package com.cinnamonbob.upgrade.tasks;

import com.cinnamonbob.model.persistence.hibernate.PersistenceTestCase;
import com.cinnamonbob.upgrade.UpgradeTask;
import com.cinnamonbob.util.jdbc.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
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
        return new String[]{"com/cinnamonbob/bootstrap/testBootstrapContext.xml",
                "com/cinnamonbob/upgrade/tasks/testSchemaUpgradeTaskContext.xml"};
    }

    public void testAddTableWithColumn() throws Exception
    {
        UpgradeTask upgrade;

        // before, test that table is not there.
        assertFalse(checkTableExists("TEST"));

        upgrade = newSchemaUpgrade("com/cinnamonbob/upgrade/tasks/testSchemaMigration-v1.hbm.xml");
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

        upgrade = newSchemaUpgrade("com/cinnamonbob/upgrade/tasks/testSchemaMigration-v1.hbm.xml");
        upgrade.execute(new MockUpgradeContext());

        // after, tet that table is there.
        assertFalse(checkColumnExists("TEST", "NEW_COLUMN"));

        upgrade = newSchemaUpgrade("com/cinnamonbob/upgrade/tasks/testSchemaMigration-v2.hbm.xml");
        upgrade.execute(new MockUpgradeContext());

        assertTrue(checkColumnExists("TEST", "NEW_COLUMN"));
    }

    private UpgradeTask newSchemaUpgrade(String mapping)
    {
        MigrateSchemaUpgradeTask task = new MigrateSchemaUpgradeTask();
        task.setMapping(mapping);
        task.setDataSource(dataSource);
        return task;
    }

    private boolean checkTableExists(String tableName) throws SQLException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            return JDBCUtils.tableExists(con, tableName);
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    private boolean checkColumnExists(String tableName, String columnName)
    {
        Connection con = null;
        CallableStatement stmt = null;
        try
        {
            con = dataSource.getConnection();
            stmt = con.prepareCall("SELECT COUNT("+columnName+") FROM " + tableName);
            stmt.execute();
            return true;
        }
        catch (SQLException e)
        {
            return false;
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(con);
        }
    }
}