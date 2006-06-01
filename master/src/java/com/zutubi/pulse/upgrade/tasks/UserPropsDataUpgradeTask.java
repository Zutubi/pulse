package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class UserPropsDataUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "User preference migration.";
    }

    public String getDescription()
    {
        return "This upgrade task migrates the user preference data out of the USER table and into the " +
                "newly created USER_PROPS table.";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        // start migrating the data.
        // a) read the data from the user table - one row at a time is fine.
        List<UserPreferenceData> data = new LinkedList<UserPreferenceData>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = con.prepareCall("select * from USER");
            rs = ps.executeQuery();
            while (rs.next())
            {
                data.add(new UserPreferenceData(rs));
            }
        }
        finally
        {
            JDBCUtils.close(ps);
            JDBCUtils.close(rs);
        }

        // b) write the data into the user_props table.
        try
        {
            ps = con.prepareStatement("insert into USER_PROPS (user_id, key, value) values (?, ?, ?)");
            for (UserPreferenceData upd : data)
            {
                JDBCUtils.setLong(ps, 1, upd.getUserId());

                // default action.
                if (upd.getDefaultAction() != null)
                {
                    JDBCUtils.setString(ps, 2, "user.defaultAction");
                    JDBCUtils.setString(ps, 3, upd.getDefaultAction());
                    ps.executeUpdate();
                }

                if (upd.getRefreshInterval() != null)
                {
                    JDBCUtils.setString(ps, 2, "user.refreshInterval");
                    JDBCUtils.setString(ps, 3, String.valueOf(upd.getRefreshInterval()));
                    ps.executeUpdate();
                }

                if (upd.getShowAllProjects() != null)
                {
                    JDBCUtils.setString(ps, 2, "user.showAllProjects");
                    JDBCUtils.setString(ps, 3, String.valueOf(upd.getShowAllProjects()));
                    ps.executeUpdate();
                }
            }
        }
        finally
        {
            JDBCUtils.close(ps);
        }

        // c) leave the data there just in case something has gone wrong.
        // noop.
    }

    /**
     * The loss of user preference data should not stop the upgrade process. The data is not deleted
     * so can be manually migrated with some assistance.
     *
     * @return false.
     */
    public boolean haltOnFailure()
    {
        return false;
    }

    private class UserPreferenceData
    {
        private String defaultAction = null;
        private Integer refreshInterval = null;
        private Boolean showAllProjects = null;

        private long userId;

        public UserPreferenceData(ResultSet rs) throws SQLException
        {
            userId = rs.getLong("id");
            defaultAction = rs.getString("defaultAction");
            refreshInterval = JDBCUtils.getInt(rs, "refreshInterval");
            showAllProjects = JDBCUtils.getBool(rs, "showAllProjects");
        }

        public long getUserId()
        {
            return userId;
        }


        public String getDefaultAction()
        {
            return defaultAction;
        }

        public Integer getRefreshInterval()
        {
            return refreshInterval;
        }

        public Boolean getShowAllProjects()
        {
            return showAllProjects;
        }
    }
}
