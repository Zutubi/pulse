package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.upgrade.UpgradeException;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * <class-comment/>
 */
public class ClonedProjectDataPatchUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private BasicDataSource dataSource;
    private Connection con;

    public ClonedProjectDataPatchUpgradeTaskTest()
    {
    }

    public ClonedProjectDataPatchUpgradeTaskTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        ComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/testBootstrapContext.xml");
        dataSource = (BasicDataSource) ComponentContext.getBean("dataSource");

        // initialise required schema.
        createSchema(dataSource, "1040");

        con = dataSource.getConnection();
    }

    protected void tearDown() throws Exception
    {
        JDBCUtils.close(con);

        JDBCUtils.execute(dataSource, "SHUTDOWN");
        dataSource.close();

        super.tearDown();
    }

    /**
     * Ensure that those projects that are okay remain that way.
     */
    public void testSingleProject() throws SQLException, UpgradeException
    {
        // create a single scm and project pair. No upgrade is required for these.
        insertScm(con, 1L);
        insertProject(con, 1L, 1L);

        executeUpgrade();

        assertScmCount(con, 1);
        assertScmsUnique(con);
    }

    public void testClonedProject() throws SQLException, UpgradeException
    {
        // create a single scm and project pair. No upgrade is required for these.
        insertScm(con, 1L);
        insertProject(con, 1L, 1L);
        insertProject(con, 2L, 1L);

        executeUpgrade();

        assertScmCount(con, 2);
        assertScmsUnique(con);
    }

    public void testMultipleClonedProject() throws SQLException, UpgradeException
    {
        // create a single scm and project pair. No upgrade is required for these.
        insertScm(con, 1L);
        insertProject(con, 1L, 1L);
        insertProject(con, 2L, 1L);
        insertProject(con, 3L, 1L);
        insertProject(con, 4L, 1L);

        executeUpgrade();

        assertScmCount(con, 4);
        assertScmsUnique(con);
    }

    private void executeUpgrade()
            throws UpgradeException
    {
        ClonedProjectDataPatchUpgradeTask task = new ClonedProjectDataPatchUpgradeTask();
        task.setDataSource(dataSource);
        task.execute(new MockUpgradeContext());
        assertEquals(0, task.getErrors().size());
    }

    private void assertScmsUnique(Connection con) throws SQLException
    {
        assertEquals(0, JDBCUtils.executeCount(con, "SELECT scm FROM project GROUP BY scm HAVING count(scm) > 1"));
    }

    private void assertScmCount(Connection con, int i) throws SQLException
    {
        assertEquals(i, JDBCUtils.executeCount(con, "SELECT * FROM scm"));
    }

    private void insertScm(Connection con, long id) throws SQLException
    {
        String path = "not important.";
        String type = "not important.";

        JDBCUtils.executeUpdate(con,
                "INSERT INTO scm (id, scmtype, path) VALUES (?, ?, ?)",
                new Object[]{id, type, path},
                new int[]{Types.BIGINT, Types.VARCHAR, Types.VARCHAR}
        );
    }

    private void insertProject(Connection con, long projectId, long scmId) throws SQLException
    {
        JDBCUtils.executeUpdate(con,
                "INSERT INTO project (id, scm) VALUES (?, ?)",
                new Object[]{projectId, scmId},
                new int[]{Types.BIGINT, Types.BIGINT}
        );
    }
}
