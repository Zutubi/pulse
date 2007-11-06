package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.DataSourceAware;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.util.JDBCUtils;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;
import org.acegisecurity.providers.encoding.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class FlexibleConditionUpgradeTask implements PulseUpgradeTask, DataSourceAware
{
    private DataSource dataSource;

    private int buildNumber;

    private List<String> errors = new LinkedList<String>();

    public String getName()
    {
        return "Update subscription conditions";
    }

    public String getDescription()
    {
        return "This upgrade task updates the format of the conditions stored with notification subscriptions";
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public void execute() throws UpgradeException
    {
        Connection con = null;
        try
        {
            con = dataSource.getConnection();

            List<IdCondition> results = new LinkedList<IdCondition>();

            // lookup all of the subscriptions, retrieving the id and condition fields.
            CallableStatement stmt = null;
            ResultSet rs = null;
            try
            {
                stmt = con.prepareCall("SELECT id, condition FROM subscription");
                rs = stmt.executeQuery();
                while (rs.next())
                {
                    results.add(new IdCondition(JDBCUtils.getLong(rs, "id"), JDBCUtils.getString(rs, "condition")));
                }
            }
            finally
            {
                JDBCUtils.close(rs);
                JDBCUtils.close(stmt);
            }

            // update the passwords.
            PasswordEncoder encoder = new Md5PasswordEncoder();

            for (IdCondition data : results)
            {
                data.condition = mapCondition(data.condition);
            }

            // update the database.
            PreparedStatement ps = null;
            try
            {
                ps = con.prepareStatement("UPDATE subscription SET condition = ? WHERE id = ?");
                for (IdCondition data : results)
                {
                    JDBCUtils.setString(ps, 1, data.condition);
                    JDBCUtils.setLong(ps, 2, data.id);


                    int rowCount = ps.executeUpdate();
                    if (rowCount != 1)
                    {
                        errors.add("Failed to update condition. Row count is " + rowCount + " where 1 was expected.");
                    }
                }
            }
            finally
            {
                JDBCUtils.close(ps);
            }
        }
        catch (SQLException e)
        {
            errors.add("SQLException: " + e.getMessage());
        }
        finally
        {
            JDBCUtils.close(con);
        }
    }

    private String mapCondition(String condition)
    {
        // Old conditions, from 1.0.3 and earlier
        final String ALL_BUILDS = "all builds";
        final String ALL_CHANGED = "all changed";
        final String ALL_FAILED = "all failed";
        final String ALL_CHANGED_OR_FAILED = "all changed or failed";
        final String ALL_FAILED_AND_FIRST_SUCCESS = "all failed and first success";

        if(condition.equals(ALL_BUILDS))
        {
            return NotifyConditionFactory.TRUE;
        }
        else if(condition.equals(ALL_CHANGED))
        {
            return NotifyConditionFactory.CHANGED;
        }
        else if(condition.equals(ALL_FAILED))
        {
            return "not " + NotifyConditionFactory.SUCCESS;
        }
        else if(condition.equals(ALL_CHANGED_OR_FAILED))
        {
            return NotifyConditionFactory.CHANGED + " or not " + NotifyConditionFactory.SUCCESS;
        }
        else if(condition.equals(ALL_FAILED_AND_FIRST_SUCCESS))
        {
            return "not " + NotifyConditionFactory.SUCCESS + " or " + NotifyConditionFactory.STATE_CHANGE;
        }
        else
        {
            return "false";
        }
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public boolean hasFailed()
    {
        return getErrors().size() > 0;
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    /**
     * Required resource.
     *
     * @param source
     */
    public void setDataSource(DataSource source)
    {
        this.dataSource = source;
    }

    private class IdCondition
    {
        protected long id;
        protected String condition;

        IdCondition(long id, String condition)
        {
            this.id = id;
            this.condition = condition;
        }
    }
}
