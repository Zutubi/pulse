/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.upgrade.UpgradeException;
import org.apache.commons.dbcp.BasicDataSource;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;

import java.sql.*;

/**
 * <class-comment/>
 */
public class EncryptPasswordUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private BasicDataSource dataSource;

    public EncryptPasswordUpgradeTaskTest()
    {
    }

    public EncryptPasswordUpgradeTaskTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        ComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/testBootstrapContext.xml");
        dataSource = (BasicDataSource) ComponentContext.getBean("dataSource");

        // initialise required schema.
        createSchema(dataSource, 1020);
    }

    protected void tearDown() throws Exception
    {
        JDBCUtils.execute(dataSource, "SHUTDOWN");
        dataSource.close();

        super.tearDown();
    }

    public void testUpgrade() throws SQLException, UpgradeException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();

            // insert data into table.
            insertTestData(con, 1L, "password");

            // upgrade
            EncryptPasswordUpgradeTask upgradeTask = new EncryptPasswordUpgradeTask();
            upgradeTask.setDataSource(dataSource);
            upgradeTask.execute(new MockUpgradeContext());

            assertEquals(0, upgradeTask.getErrors().size());

            // extract data
            String password = selectPassword(con, 1L);

            // verify that it is as expected.
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();
            assertTrue(encoder.isPasswordValid(password, "password", null));
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    private String selectPassword(Connection con, Long id) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareCall("SELECT password FROM user WHERE id = ?");
            JDBCUtils.setLong(stmt, 1, id);
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return JDBCUtils.getString(rs, "password");
            }
            return null;
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void insertTestData(Connection con, Long id, String password) throws SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement("insert into USER (id, password) values (?, ?)");
            JDBCUtils.setLong(ps, 1, id);
            JDBCUtils.setString(ps, 2, password);
            ps.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(ps);
        }
    }
}
