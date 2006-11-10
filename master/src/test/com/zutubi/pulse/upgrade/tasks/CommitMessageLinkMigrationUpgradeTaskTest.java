package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.util.PropertiesType;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.committransformers.StandardCommitMessageTransformer;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.*;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;

/**
 * <class comment/>
 */
public class CommitMessageLinkMigrationUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private BasicDataSource dataSource;
    private Connection con;

    protected void setUp() throws Exception
    {
        super.setUp();

        ComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/testBootstrapContext.xml");
        dataSource = (BasicDataSource) ComponentContext.getBean("dataSource");

        // initialise required schema.
        createSchema(dataSource, "0102001000");

        con = dataSource.getConnection();
    }

    protected void tearDown() throws Exception
    {
        JDBCUtils.close(con);

        JDBCUtils.execute(dataSource, "SHUTDOWN");
        dataSource.close();

        super.tearDown();
    }

    public void testNoData() throws UpgradeException
    {
        List<String> columns = Arrays.asList(JDBCUtils.getSchemaColumnNames(con, "COMMIT_MESSAGE_TRANSFORMER"));
        assertFalse(columns.contains("PROPERTIES"));
        assertFalse(columns.contains("TYPE"));
        assertTrue(columns.contains("EXPRESSION"));
        assertTrue(columns.contains("REPLACEMENT"));

        executeUpgrade();

        columns = Arrays.asList(JDBCUtils.getSchemaColumnNames(con, "COMMIT_MESSAGE_TRANSFORMER"));
        assertTrue(columns.contains("PROPERTIES"));
        assertTrue(columns.contains("TYPE"));
        assertFalse(columns.contains("EXPRESSION"));
        assertFalse(columns.contains("REPLACEMENT"));
    }

    public void testSingleLinker() throws UpgradeException, SQLException
    {
        insertLinker(con, 1, "expression", "replacement");

        executeUpgrade();

        checkConversion(con, 1, "expression", "replacement");
    }

    private void checkConversion(Connection con, long id, String expression, String link) throws SQLException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = con.prepareStatement("SELECT * FROM commit_message_transformer WHERE id = ?");
            JDBCUtils.setLong(ps, 1, id);
            rs = ps.executeQuery();
            assertTrue(rs.next());

            PropertiesType type = new PropertiesType();
            Properties props = (Properties) type.fromStringValue(rs.getString("PROPERTIES"));
            assertEquals(expression, props.getProperty(StandardCommitMessageTransformer.EXPRESSION_PROPERTY));
            assertEquals(link, props.getProperty(StandardCommitMessageTransformer.LINK_PROPERTY));
            assertEquals("STANDARD", rs.getString("TYPE"));
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }
    }

    private void insertLinker(Connection con, long id, String expression, String replacement) throws SQLException
    {
        JDBCUtils.executeUpdate(con,
                "INSERT INTO commit_message_transformer (id, expression, replacement) VALUES (?, ?, ?)",
                new Object[]{id, expression, replacement},
                new int[]{Types.BIGINT, Types.VARCHAR, Types.VARCHAR}
        );
    }

    private void executeUpgrade() throws UpgradeException
    {
        // execute schema upgrade:
        MigrateSchemaUpgradeTask schemaUpgrade = new MigrateSchemaUpgradeTask();
        schemaUpgrade.setMapping("com/zutubi/pulse/upgrade/schema/build_0102001007/CommitMessageTransformer.hbm.xml");
        schemaUpgrade.setDataSource(dataSource);
        schemaUpgrade.setBuildNumber(102001007);
        schemaUpgrade.execute(new MockUpgradeContext());

        CommitMessageLinkMigrationUpgradeTask task = new CommitMessageLinkMigrationUpgradeTask();
        task.setDataSource(dataSource);
        task.execute(new MockUpgradeContext());
        assertEquals(0, task.getErrors().size());
    }

}
