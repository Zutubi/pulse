package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.io.IOException;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 */
public class TriggerForceUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Trigger force";
    }

    public String getDescription()
    {
        return "Upgrade to triggers required for changelist isolation";
    }

    public void execute(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, data FROM trigger WHERE task_class = 'com.zutubi.pulse.scheduling.tasks.BuildProjectTask' AND trigger_event IS NULL OR trigger_event != 'com.zutubi.pulse.scm.SCMChangeEvent'");
            rs = stmt.executeQuery();

            while (rs.next())
            {
                try
                {
                    updateDataMap(con, rs);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    errors.add(e.getMessage());
                    return;
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                    errors.add(e.getMessage());
                    return;
                }
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private void updateDataMap(Connection con, ResultSet rs) throws IOException, ClassNotFoundException, SQLException
    {
        byte[] data = upgradeBlob(rs, "data", new ObjectUpgrader()
        {
            public void upgrade(Object object)
            {
                Map<Serializable, Serializable> dataMap = (Map<Serializable, Serializable>) object;
                dataMap.put(BuildProjectTask.PARAM_FORCE, true);
            }
        });

        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("UPDATE trigger SET data = ? WHERE id = ?");
            stmt.setBytes(1, data);
            stmt.setLong(2, JDBCUtils.getLong(rs, "id"));
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        // No going on if the schema isn't updated
        return true;
    }
}
