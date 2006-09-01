package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

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
public class DashboardProjectsUpgradeTask extends DatabaseUpgradeTask
{
    public String getName()
    {
        return "Dashboard projects";
    }

    public String getDescription()
    {
        return "Upgrade to convert to a new scheme for dashboard project preferences";
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        updateProperties(con);
        dropColumns(con);
    }

    private void updateProperties(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        ResultSet rs = null;

        Map<Long, List<Long>> userProjects = new TreeMap<Long, List<Long>>();

        try
        {
            stmt = con.prepareCall("SELECT id, user_id FROM project where user_id is not null");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                long project = rs.getLong("id");
                long user = rs.getLong("user_id");

                List<Long> projects = userProjects.get(user);
                if(projects == null)
                {
                    projects = new LinkedList<Long>();
                    userProjects.put(user, projects);
                }

                projects.add(project);
            }

            if(userProjects.size() > 0)
            {
                invertUserProjects(con, userProjects);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void invertUserProjects(Connection con, Map<Long, List<Long>> userProjects) throws SQLException
    {
        List<Long> allProjects = getAllProjects(con);
        for(Map.Entry<Long, List<Long>> entry: userProjects.entrySet())
        {
            List<Long> hidden = new LinkedList<Long>(allProjects);
            hidden.removeAll(entry.getValue());
            addHiddenProjects(con, entry.getKey(), hidden);
        }
    }

    private void addHiddenProjects(Connection con, Long user, List<Long> hidden) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("INSERT INTO user_hidden_projects VALUES (?, ?)");
            for(Long project: hidden)
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

    private void dropColumns(Connection con) throws SQLException
    {
        CallableStatement stmt = null;
        try
        {
            stmt = con.prepareCall("ALTER TABLE user DROP COLUMN showallprojects");
            stmt.executeUpdate();
            JDBCUtils.close(stmt);
            stmt = con.prepareCall("UPDATE project SET user_id = null");
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
