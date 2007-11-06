package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class DashboardProjectGroupsUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Dashboard project groups";
    }

    public String getDescription()
    {
        return "Upgrade to allow project groups to be chosen for display on the dashboard";
    }

    public void execute(Connection con) throws SQLException, IOException
    {
        updateUsers(con, getAllUsers(con), getAllProjects(con));
        dropTable(con);
    }

    private void updateUsers(Connection con, List<Long> allUsers, List<Long> allProjects) throws SQLException, IOException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        Map<Long, List<Long>> userHiddenProjects = new TreeMap<Long, List<Long>>();

        try
        {
            stmt = con.prepareCall("SELECT user_id, project_id FROM user_hidden_projects");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                long user = rs.getLong("user_id");
                long project = rs.getLong("project_id");

                List<Long> projects = userHiddenProjects.get(user);
                if(projects == null)
                {
                    projects = new LinkedList<Long>();
                    userHiddenProjects.put(user, projects);
                }

                projects.add(project);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        for(Long user: allUsers)
        {
            updateUser(con, user, userHiddenProjects.get(user), allProjects);
        }
    }

    private void updateUser(Connection con, Long user, List<Long> hidden, List<Long> allProjects) throws SQLException, IOException
    {
        boolean showAll = hidden == null || hidden.size() == 0;
        if(!showAll)
        {
            invertUserProjects(con, user, hidden, allProjects);
        }

        CallableStatement stmt = null;

        try
        {
            stmt = con.prepareCall("INSERT INTO user_props VALUES (?, ?, ?)");
            stmt.setLong(1, user);
            stmt.setString(2, Boolean.toString(showAll));
            stmt.setString(3, User.PROPERTY_SHOW_ALL_PROJECTS);

            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void invertUserProjects(Connection con, Long user, List<Long> hidden, List<Long> allProjects) throws SQLException
    {
        List<Long> shown = new LinkedList<Long>(allProjects);
        shown.removeAll(hidden);

        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("INSERT INTO user_shown_projects VALUES (?, ?)");
            for(Long project: shown)
            {
                stmt.setLong(1, user);
                stmt.setLong(2, project);
                stmt.executeUpdate();
            }
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void dropTable(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("DROP TABLE user_hidden_projects");
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
