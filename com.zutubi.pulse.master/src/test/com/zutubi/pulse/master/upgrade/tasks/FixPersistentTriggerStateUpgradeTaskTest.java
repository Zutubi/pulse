package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.util.JDBCUtils;

import java.util.List;
import java.util.Arrays;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;

public class FixPersistentTriggerStateUpgradeTaskTest extends BaseUpgradeTaskTestCase
{
    private FixPersistentTriggerStateUpgradeTask task;

    protected void setUp() throws Exception
    {
        super.setUp();

        task = new FixPersistentTriggerStateUpgradeTask();
        task.setDataSource(dataSource);
    }

    public void testUpgrade() throws SQLException, IOException
    {
        insertTriggerState(1, "NONE");
        insertTriggerState(2, "PAUSED");
        insertTriggerState(3, "SCHEDULED");

        runUpgrade();
        
        assertTriggerState(1, "SCHEDULED");
        assertTriggerState(2, "PAUSED");
        assertTriggerState(3, "SCHEDULED");
    }

    private void assertTriggerState(long id, String state) throws SQLException
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            con = dataSource.getConnection();
            ps = con.prepareStatement("SELECT state FROM local_trigger WHERE id = ?");
            JDBCUtils.setLong(ps, 1, id);
            rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(state, JDBCUtils.getString(rs, "state"));
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
            JDBCUtils.close(con);
        }

    }

    private void insertTriggerState(long id, String state) throws SQLException
    {
        JDBCUtils.execute(dataSource, "INSERT INTO local_trigger (id, state, trigger_type, trigger_name, trigger_group) values ("+id+", '"+state+"', 'x', 'x', 'x')");
    }

    private void runUpgrade() throws SQLException, IOException
    {
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
    }

    protected List<String> getMappings()
    {
        return Arrays.asList("com/zutubi/pulse/master/upgrade/tasks/schema/Schema-2.0.13-mappings.hbm.xml");
    }
}
