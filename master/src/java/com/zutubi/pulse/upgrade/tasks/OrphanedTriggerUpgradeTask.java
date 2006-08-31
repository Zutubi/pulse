package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.List;
import java.io.IOException;

/**
 */
public class OrphanedTriggerUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Orphaned triggers";
    }

    public String getDescription()
    {
        return "Deletes any orphaned triggers";
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        List<Long> projects = getAllProjects(con);

        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, project FROM trigger");
            rs = stmt.executeQuery();

            while (rs.next())
            {
                Long project = JDBCUtils.getLong(rs, "project");
                if(project != 0 && !projects.contains(project))
                {
                    deleteTrigger(con, JDBCUtils.getLong(rs, "id"));
                }
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(rs);
        }
    }

    private void deleteTrigger(Connection con, Long id) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("DELETE FROM trigger where id = ?");
            stmt.setLong(1, id);
            stmt.execute();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }
}
