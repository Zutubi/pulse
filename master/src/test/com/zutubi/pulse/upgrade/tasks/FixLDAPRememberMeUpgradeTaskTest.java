package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.util.JDBCUtils;

import javax.sql.DataSource;
import java.util.List;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Types;

import org.acegisecurity.providers.encoding.Md5PasswordEncoder;

/**
 *
 *
 */
public class FixLDAPRememberMeUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    public FixLDAPRememberMeUpgradeTaskTest()
    {
    }

    public FixLDAPRememberMeUpgradeTaskTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    protected List<String> getTestMappings()
    {
        return getMappings("0102056001");
    }

    public void testUpgrade() throws UpgradeException, SQLException
    {
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();

        Connection con = null;
        try
        {
            con = dataSource.getConnection();
            insertTestData(con, 1L, encoder.encodePassword("password", null));
            insertTestData(con, 2L, encoder.encodePassword(null, null));
        }
        finally
        {
            JDBCUtils.close(con);
        }

        FixLDAPRememberMeUpgradeTask dataUpgrade = new FixLDAPRememberMeUpgradeTask();
        dataUpgrade.setDataSource(dataSource);
        dataUpgrade.execute(new MockUpgradeContext());

        assertFalse(dataUpgrade.hasFailed());

        // check that the changes are as expected.
        try
        {
            con = dataSource.getConnection();
            assertPasswordsEqual(con, 1L, encoder.encodePassword("password", null));
            assertPasswordsNotEqual(con, 1L, encoder.encodePassword(null, null));
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    private void assertPasswordsEqual(Connection con, Long userId, String expectedPassword) throws SQLException
    {
        assertEquals(expectedPassword, JDBCUtils.executeSimpleQuery(con, "select password from LOCAL_USER where id = "+ userId));
    }

    private void assertPasswordsNotEqual(Connection con, Long userId, String expectedPassword) throws SQLException
    {
        assertNotSame(expectedPassword, JDBCUtils.executeSimpleQuery(con, "select password from LOCAL_USER where id = "+ userId));
    }

    private void insertTestData(Connection con, Long id, String password) throws SQLException
    {
        JDBCUtils.executeUpdate(con,
                "insert into local_user (id, password) values (?, ?)",
                new Object[]{id, password},
                new int[]{Types.BIGINT, Types.VARCHAR}
        );
    }

}
