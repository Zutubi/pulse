package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CollapseRevisionRefactorUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private CollapseRevisionRefactorUpgradeTask task;

    protected void setUp() throws Exception
    {
        super.setUp();

        task = new CollapseRevisionRefactorUpgradeTask();
        task.setDataSource(dataSource);
        task.setHibernateProperties(databaseConfig.getHibernateProperties());
        task.setMappings(getMappings());
    }

    protected void tearDown() throws Exception
    {
        task = null;
        
        super.tearDown();
    }

    public void testEmptyDatabaseUpgrade() throws SQLException, IOException
    {
        assertTableExists("REVISION");
        assertColumnExists("BUILD_CHANGELIST", "REVISION_AUTHOR");
        assertColumnExists("BUILD_CHANGELIST", "REVISION_COMMENT");
        assertColumnExists("BUILD_CHANGELIST", "REVISION_DATE");
        assertColumnExists("BUILD_CHANGELIST", "REVISION_BRANCH");
        assertColumnExists("BUILD_CHANGELIST", "HASH");
        assertColumnExists("BUILD_RESULT", "REVISION_ID");
        assertColumnNotExists("BUILD_RESULT", "REVISION_STRING");

        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            task.execute(con);
            assertFalse(task.hasFailed());
        }
        finally
        {
            JDBCUtils.close(con);
        }

        assertTableNotExists("REVISION");
        assertColumnExists("BUILD_RESULT", "REVISION_STRING");
        assertColumnNotExists("BUILD_CHANGELIST", "REVISION_AUTHOR");
        assertColumnNotExists("BUILD_CHANGELIST", "REVISION_COMMENT");
        assertColumnNotExists("BUILD_CHANGELIST", "REVISION_DATE");
        assertColumnNotExists("BUILD_CHANGELIST", "REVISION_BRANCH");
        assertColumnNotExists("BUILD_CHANGELIST", "HASH");
        assertColumnNotExists("BUILD_RESULT", "REVISION_ID");
    }

    public void assertTableExists(String tableName)
    {
        assertTrue(JDBCUtils.tableExists(dataSource, tableName));
    }

    public void assertTableNotExists(String tableName)
    {
        assertFalse(JDBCUtils.tableExists(dataSource, tableName));
    }

    public void assertColumnExists(String tableName, String columnName)
    {
        assertTrue(JDBCUtils.columnExists(dataSource, tableName, columnName));
    }

    public void assertColumnNotExists(String tableName, String columnName)
    {
        assertFalse(JDBCUtils.columnExists(dataSource, tableName, columnName));
    }

    protected List<String> getMappings()
    {
        return Arrays.asList("com/zutubi/pulse/master/upgrade/tasks/schema/Schema-2.0.13-mappings.hbm.xml");
    }

}
