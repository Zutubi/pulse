package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.util.JDBCUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Test case for the User Property upgrade process.
 *
 * @author Daniel Ostermeier
 */
public class UserPropsUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    public UserPropsUpgradeTaskTest()
    {
    }

    public UserPropsUpgradeTaskTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        generateTestData(dataSource);
    }

    protected List<String> getTestMappings()
    {
        return getMappings("1010");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testUpgrade() throws UpgradeException, SQLException
    {
        // pre upgrade check.
        assertFalse(JDBCUtils.tableExists(dataSource, "USER_PROPS"));

        // upgrade schema
        UserPropsSchemaUpgradeTask schemaUpgrade = new UserPropsSchemaUpgradeTask();
        schemaUpgrade.setDataSource(dataSource);
        schemaUpgrade.setDatabaseConfig(databaseConfig);
        schemaUpgrade.execute();

        // check that the new table exists as expected
        assertTrue(JDBCUtils.tableExists(dataSource, "USER_PROPS"));

        // run the data migration.
        UserPropsDataUpgradeTask dataUpgrade = new UserPropsDataUpgradeTask();
        dataUpgrade.setDataSource(dataSource);
        dataUpgrade.execute();


        // check that the data is as expected.
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            assertRowExists(con, 1L, "user.defaultAction", "defaultAction.action");
            assertRowExists(con, 1L, "user.refreshInterval", "30");
            assertRowExists(con, 1L, "user.showAllProjects", "true");

            // user 2.
            assertRowNotExists(con, 2L, "user.defaultAction");
            assertRowExists(con, 2L, "user.refreshInterval", "60");
            assertRowExists(con, 2L, "user.showAllProjects", "false");

            // user 3.
            assertRowNotExists(con, 3L, "user.defaultAction");
            assertRowNotExists(con, 3L, "user.refreshInterval");
            assertRowNotExists(con, 3L, "user.showAllProjects");
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    private void assertRowExists(Connection con, Long userId, String key, String value) throws SQLException
    {
        assertEquals(1, JDBCUtils.executeCount(con,
                "select 1 from USER_PROPS where user_id = ? AND key = ? AND value = ?",
                new Object[]{userId, key, value},
                new int[]{Types.BIGINT, Types.VARCHAR, Types.VARCHAR}
        ));
    }

    private void assertRowNotExists(Connection con, Long userId, String key) throws SQLException
    {
        assertEquals(0, JDBCUtils.executeCount(con,
                "select 1 from USER_PROPS where user_id = ? AND key = ?",
                new Object[]{userId, key},
                new int[]{Types.BIGINT, Types.VARCHAR}
        ));
    }

    public void generateTestData(DataSource dataSource) throws SQLException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            insertTestData(con, 1L, "defaultAction.action", 30, true);
            insertTestData(con, 2L, null, 60, false);
            insertTestData(con, 3L, null, null, null);
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    private void insertTestData(Connection con, Long id, String defaultAction, Integer refreshInterval, Boolean showAllProjects) throws SQLException
    {
        JDBCUtils.executeUpdate(con,
                "insert into USER (id, defaultAction, refreshInterval, showAllProjects) values (?, ?, ?, ?)",
                new Object[]{id, defaultAction, refreshInterval, showAllProjects},
                new int[]{Types.BIGINT, Types.VARCHAR, Types.INTEGER, Types.BIT}
        );
    }

}
