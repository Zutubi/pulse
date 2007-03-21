package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.*;

/**
 */
public class SubscriptionConditionUpgradeTask extends DatabaseUpgradeTask
{
    private long nextId;

    public String getName()
    {
        return "Subscription conditions";
    }

    public String getDescription()
    {
        return "Upgrades subscriptions to allow common cases to be configured more easily";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        // All subscriptions with TYPE PROJECT should have their
        // NOTIFY_CONDITION column converted to a row in the
        // PROJECT_BUILD_CONDITION table with TYPE ADVANCED and
        // ADVANCED_CONDITION equal to the NOTIFY_CONDITION.  We can then
        // drop the NOTIFY_CONDITION column.
        nextId = HibernateUtils.getNextId(con);
        convertConditions(con);
        dropColumn(con);
    }

    private void dropColumn(Connection con) throws SQLException
    {
        PreparedStatement stmt = null;

        try
        {
            stmt = con.prepareStatement("ALTER TABLE SUBSCRIPTION DROP COLUMN NOTIFY_CONDITION");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void convertConditions(Connection con) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareStatement("SELECT ID, NOTIFY_CONDITION FROM SUBSCRIPTION WHERE TYPE='PROJECT'");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                Long id = JDBCUtils.getLong(rs, "ID");
                String expression = JDBCUtils.getString(rs, "NOTIFY_CONDITION");
                Long conditionId = addCondition(con, id, expression);
                updateSubscription(con, id, conditionId);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private Long addCondition(Connection con, Long id, String expression) throws SQLException
    {
        PreparedStatement stmt = null;

        try
        {
            Long newId = nextId++;
            stmt = con.prepareStatement("INSERT INTO PROJECT_BUILD_CONDITION (ID, CONDITION_TYPE, ADVANCED_EXPRESSION) VALUES (?, ?, ?)");
            stmt.setLong(1, newId);
            stmt.setString(2, "ADVANCED");
            stmt.setString(3, expression);
            stmt.executeUpdate();
            return newId;
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void updateSubscription(Connection con, Long id, Long conditionId) throws SQLException
    {
        PreparedStatement stmt = null;

        try
        {
            stmt = con.prepareStatement("UPDATE SUBSCRIPTION SET CONDITION_ID = ? WHERE ID = ?");
            stmt.setLong(1, conditionId);
            stmt.setLong(2, id);
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
