package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class DistributedUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "1.1 data updates";
    }

    public String getDescription()
    {
        return "Data upgrade for 1.1 release stream";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        // A hibernate schema upgrade task is used to add the new tables before
        // this task runs.

        // - added buildReason to BuildResult
        //   - new table, all current results get "unknown"
        // - added name to BuildStage
        //   - all current stages receive name "default"
        // - added stage to RecipeResultNode
        //   - all current results get "default"
        // - added ResourceRequirements to nodes
        //   - a new table
        // - added AnyCapableBuildHostRequirements
        //   - nothing to do
        // - renamed Resource to PersistentResource, added slave
        //   - rename is fine, new null column is added by hibernate task
        // - removed status, last ping from Slave
        //   - drop these columns

        addBuildReasons(con);
        addStageNames(con);
        addResultStageNames(con);
        removeSlaveColumns(con);
    }

    private void addBuildReasons(Connection con)
            throws SQLException
    {
        List<Long> buildIds = new LinkedList<Long>();

        CallableStatement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.prepareCall("SELECT id FROM build_result");
            rs = stmt.executeQuery();
            while (rs.next())
            {
                buildIds.add(JDBCUtils.getLong(rs, "id"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        CallableStatement reasonStmt = null;
        stmt = null;
        long nextId = HibernateUtils.getNextId(con);

        try
        {
            stmt = con.prepareCall("UPDATE build_result SET reason = ? where id = ?");
            reasonStmt = con.prepareCall("INSERT INTO build_reason (id, reason_type) values (?, 'UNKNOWN')");

            for (Long id : buildIds)
            {
                // Insert the reason
                JDBCUtils.setLong(reasonStmt, 1, nextId);
                reasonStmt.executeUpdate();

                // Add the reason to the result
                JDBCUtils.setLong(stmt, 1, nextId);
                JDBCUtils.setLong(stmt, 2, id);
                stmt.executeUpdate();

                nextId++;
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
            JDBCUtils.close(reasonStmt);
        }
    }

    private void addStageNames(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("ALTER TABLE build_specification_node ADD COLUMN name VARCHAR(255) DEFAULT 'default'");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void addResultStageNames(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("ALTER TABLE recipe_result_node ADD COLUMN stage VARCHAR(255) DEFAULT 'default'");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void removeSlaveColumns(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("ALTER TABLE slave DROP COLUMN last_ping_time");
            stmt.executeUpdate();
            stmt = con.prepareCall("ALTER TABLE slave DROP COLUMN status");
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
