package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.util.JDBCUtils;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * <class-comment/>
 */
public class ScmMonitorUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    public ScmMonitorUpgradeTaskTest()
    {
    }

    public ScmMonitorUpgradeTaskTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // create a couple of test scms configurations.
        generateTestData(dataSource);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    protected List<String> getTestMappings()
    {
        return getMappings("1040");
    }

    public void testUpgrade() throws UpgradeException, SQLException
    {
        // upgrade schema
        ScmMonitorSchemaUpgradeTask schemaUpgrade = new ScmMonitorSchemaUpgradeTask();
        schemaUpgrade.setDataSource(dataSource);
        schemaUpgrade.setDatabaseConfig(databaseConfig);
        schemaUpgrade.execute();

        // run the data migration.
        ScmMonitorDataUpgradeTask dataUpgrade = new ScmMonitorDataUpgradeTask();
        dataUpgrade.setDataSource(dataSource);
        dataUpgrade.execute();

        // ensure that all scms have correct monitor settings.
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            assertMonitorTrue(con, 1L);
            assertMonitorTrue(con, 2L);
            assertMonitorTrue(con, 3L);
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    private void assertMonitorTrue(Connection con, long l) throws SQLException
    {
        assertEquals(1, JDBCUtils.executeCount(con,
                "SELECT monitor FROM scm WHERE id = ?",
                new Object[]{l},
                new int[]{Types.BIGINT}
        ));
    }

    private void generateTestData(BasicDataSource dataSource) throws SQLException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            String sql = "INSERT INTO scm (id, scmtype) values (?, ?)";
            JDBCUtils.executeUpdate(con, sql, new Object[]{1L, "noop"}, new int[]{Types.BIGINT, Types.VARCHAR});
            JDBCUtils.executeUpdate(con, sql, new Object[]{2L, "noop"}, new int[]{Types.BIGINT, Types.VARCHAR});
            JDBCUtils.executeUpdate(con, sql, new Object[]{3L, "noop"}, new int[]{Types.BIGINT, Types.VARCHAR});
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

}
