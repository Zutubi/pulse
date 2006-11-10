package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <class-comment/>
 */
public class SubscriptionProjectsUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Subscription projects";
    }

    public String getDescription()
    {
        return "Upgrades subscriptions to allow them to refer to multiple projects";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        // All current subscriptions have exactly one project.  Move it into the new
        // SUBSCRIPTION_PROJECTS table, then drop the PROJECT_ID column from the
        // SUBSCRIPTIONS table.
        addProjects(con);
        dropColumn(con);
    }

    private void dropColumn(Connection con) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            // Can't drop as we can't cascade the constraint :|
            stmt = con.prepareCall("UPDATE subscription SET project_id = null");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void addProjects(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareCall("SELECT id, project_id FROM subscription");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                Long id = JDBCUtils.getLong(rs, "id");
                Long project = JDBCUtils.getLong(rs, "project_id");

                addProject(con, id, project);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void addProject(Connection con, Long subscription, Long project) throws SQLException
    {
        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("INSERT INTO subscription_projects VALUES (?, ?)");
            stmt.setLong(1, subscription);
            stmt.setLong(2, project);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
