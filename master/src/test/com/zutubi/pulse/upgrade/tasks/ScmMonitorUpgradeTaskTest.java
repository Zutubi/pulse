/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.util.JDBCUtils;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * <class-comment/>
 */
public class ScmMonitorUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private BasicDataSource dataSource;

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

        ComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/testBootstrapContext.xml");
        dataSource = (BasicDataSource) ComponentContext.getBean("dataSource");

        // initialise required schema.
        createSchema(dataSource, 1040);

        // create a couple of test scms configurations.
        generateTestData(dataSource);
    }

    protected void tearDown() throws Exception
    {
        JDBCUtils.execute(dataSource, "SHUTDOWN");
        dataSource.close();

        super.tearDown();
    }

    public void testUpgrade() throws UpgradeException, SQLException
    {
        // upgrade schema
        ScmMonitorSchemaUpgradeTask schemaUpgrade = new ScmMonitorSchemaUpgradeTask();
        schemaUpgrade.setDataSource(dataSource);
        schemaUpgrade.execute(new MockUpgradeContext());

        // run the data migration.
        ScmMonitorDataUpgradeTask dataUpgrade = new ScmMonitorDataUpgradeTask();
        dataUpgrade.setDataSource(dataSource);
        dataUpgrade.execute(new MockUpgradeContext());

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
        PreparedStatement ps = null;
        ResultSet rs;
        try
        {
            ps = con.prepareStatement("SELECT monitor FROM scm WHERE id = ?");
            JDBCUtils.setLong(ps, 1, l);
            rs = ps.executeQuery();
            if (rs.next())
            {
                assertTrue(JDBCUtils.getBool(rs, "monitor"));
            }
            else
            {
                fail();
            }
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }

    private void generateTestData(BasicDataSource dataSource) throws SQLException
    {
        Connection con = null;
        PreparedStatement ps;
        try
        {
            con = dataSource.getConnection();
            ps = con.prepareStatement("INSERT INTO scm (id, scmtype) values (?, ?)");
            JDBCUtils.setLong(ps, 1, 1L);
            JDBCUtils.setString(ps, 2, "noop");
            ps.executeUpdate();
            JDBCUtils.setLong(ps, 1, 2L);
            JDBCUtils.setString(ps, 2, "noop");
            ps.executeUpdate();
            JDBCUtils.setLong(ps, 1, 3L);
            JDBCUtils.setString(ps, 2, "noop");
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

}
