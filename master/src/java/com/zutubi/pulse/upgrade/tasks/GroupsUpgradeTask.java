package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class GroupsUpgradeTask extends DatabaseUpgradeTask
{
    private long nextId;

    public String getName()
    {
        return "Groups";
    }

    public String getDescription()
    {
        return "Adds user groups, assigning current privileges appropriately";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute(UpgradeContext context, Connection con) throws SQLException
    {
        // Current privileges are server admin and project administrators.  Schema:
        //
        //   AUTHORITIES (id, user_id: fk->USER, authority: string)
        //   PROJECT_ACL_ENTRY (id, recipient: string, mask: int, project_id: fk->PROJECT)
        //
        // Server admin:
        //   - granted by adding ROLE_ADMINISTRATOR for the user to AUTHORITIES
        //   - the admin user has ROLE_ADMINISTRATOR just like all server admins
        // Project administrators:
        //   - granted by adding user's login as recipient in a PROJECT_ACL_ENTRY
        //
        // Now all privileges will be granted to roles and groups (not logins).
        // Schema (notice AUTHORITIES is toast):
        //
        //   GROUP (id, name: string, adminAllProjects: boolean)
        //   GROUP_AUTHORITIES (id, group_id: fk->GROUP, authority: string)
        //   GROUP_USERS (group_id: fk->GROUP, user_id: fk->USER)
        //   PROJECT_ACL_ENTRY (id, recipient: string, mask: int, project_id: fk->PROJECT)
        //   USER_AUTHORITIES (id, user_id: fk->USER, authority: string)
        //
        // All groups get an authority GROUP_<group id> by default.  A group is granted
        // project admin privileges by adding this authority as recipient in a
        // PROJECT_ACL_ENTRY for that project.
        //
        // Conversion of current privileges:
        //   - admin user: granted ROLE_ADMINISTRATOR directly as before, but in the
        //     USER_AUTHORITIES table
        //   - other server admins: add to a new "admins" group which has authority
        //     ROLE_ADMINISTRATOR in GROUP_AUTHORITIES
        //   - project admins:
        //      - admins for all projects: added to new "project admins" group which
        //        is recipient of a PROJECT_ACL_ENTRY for every project
        //      - other project admins: added to new "<project name> admins" group
        //        which is recipient of a PROJECT_ACL_ENTRY for the specific project
        //
        // Once done the AUTHORITIES table should be dropped.

        nextId = HibernateUtils.getNextId(con);
        addAuthority(con, 1, "ROLE_ADMINISTRATOR");
        createAdminsGroup(con);

        Map<Long, UserInfo> users = getAllUsers(con);
        addUserAuthorities(con, users);

        Map<Long, ProjectInfo> projects = getProjects(con);

        clearAcls(con);

        List<Long> allProjectAdmins = determineAllProjectAdmins(users, projects);
        createAllProjectAdminsGroup(con, projects, allProjectAdmins);

        for(ProjectInfo project: projects.values())
        {
            List<Long> members = determineProjectAdmins(users, project);
            if(members.size() > 0)
            {
                createProjectAdminsGroup(con, project, members);
            }
        }

        dropAuthorities(con);
    }

    private void addAuthority(Connection con, long userId, String authority) throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement("INSERT INTO user_authorities (id, user_id, authority) VALUES (?, ?, ?)");
            stmt.setLong(1, nextId++);
            stmt.setLong(2, userId);
            stmt.setString(3, authority);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void addUserAuthorities(Connection con, Map<Long, UserInfo> users) throws SQLException
    {
        for(Long user: users.keySet())
        {
            addAuthority(con, user, "ROLE_USER");
        }
    }

    private void createAdminsGroup(Connection con) throws SQLException
    {
        long adminGroupId = nextId++;
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement("INSERT INTO groups (id, name, admin_all_projects) VALUES (?, 'administrators', false)");
            stmt.setLong(1, adminGroupId);
            stmt.executeUpdate();
            JDBCUtils.close(stmt);

            stmt = con.prepareStatement("INSERT INTO group_authorities (id, group_id, authority) VALUES (?, ?, 'ROLE_ADMINISTRATOR')");
            stmt.setLong(1, nextId++);
            stmt.setLong(2, adminGroupId);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }

        addServerAdmins(con, adminGroupId);
    }

    private void addServerAdmins(Connection con, long adminGroupId) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareStatement("SELECT user_id FROM AUTHORITIES WHERE authority = 'ROLE_ADMINISTRATOR'");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                Long userId = rs.getLong("user_id");
                if(userId != 1)
                {
                    addUserToGroup(con, adminGroupId, userId);
                }
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void addUserToGroup(Connection con, long groupId, long userId) throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement("INSERT INTO group_users (group_id, user_id) VALUES (?, ?)");
            stmt.setLong(1, groupId);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private Map<Long, UserInfo> getAllUsers(Connection con) throws SQLException
    {
        Map<Long, UserInfo> result = new HashMap<Long, UserInfo>();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareStatement("SELECT id, login FROM user");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                long id = rs.getLong("id");
                UserInfo info = new UserInfo(id, rs.getString("login"));
                getUserProjects(con, info);
                result.put(id, info);
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        return result;
    }

    private Map<Long, ProjectInfo> getProjects(Connection con) throws SQLException
    {
        Map<Long, ProjectInfo> result = new HashMap<Long, ProjectInfo>();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareStatement("SELECT id, name FROM project");
            rs = stmt.executeQuery();
            while(rs.next())
            {
                long id = rs.getLong("id");
                result.put(id, new ProjectInfo(id, rs.getString("name")));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }

        return result;
    }

    private void getUserProjects(Connection con, UserInfo info) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = con.prepareStatement("SELECT project_id FROM project_acl_entry WHERE recipient = ?");
            stmt.setString(1, info.login);
            rs = stmt.executeQuery();
            while(rs.next())
            {
                info.projects.add(rs.getLong("project_id"));
            }
        }
        finally
        {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
        }
    }

    private void clearAcls(Connection con) throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement("DELETE FROM project_acl_entry");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private List<Long> determineAllProjectAdmins(Map<Long, UserInfo> users, Map<Long, ProjectInfo> projects)
    {
        List<Long> result = new LinkedList<Long>();

        for(UserInfo user: users.values())
        {
            if(user.projects.size() == projects.size())
            {
                user.isAllProjectAdmin = true;
                result.add(user.id);
            }
        }

        return result;
    }

    private void createAllProjectAdminsGroup(Connection con, Map<Long, ProjectInfo> projects, List<Long> members) throws SQLException
    {
        long groupId = nextId++;
        PreparedStatement stmt = null;

        try
        {
            stmt = con.prepareStatement("INSERT INTO groups (id, name, admin_all_projects) VALUES (?, 'project administrators', true)");
            stmt.setLong(1, groupId);
            stmt.executeUpdate();
            JDBCUtils.close(stmt);
        }
        finally
        {
            JDBCUtils.close(stmt);
        }

        for(Long projectId: projects.keySet())
        {
            addAcl(con, projectId, groupId);
        }

        for(Long userId: members)
        {
            addUserToGroup(con, groupId, userId);
        }
    }

    private List<Long> determineProjectAdmins(Map<Long, UserInfo> users, ProjectInfo project)
    {
        List<Long> result = new LinkedList<Long>();

        for(UserInfo user: users.values())
        {
            if(!user.isAllProjectAdmin && user.projects.contains(project.id))
            {
                result.add(user.id);
            }
        }

        return result;
    }

    private void createProjectAdminsGroup(Connection con, ProjectInfo project, List<Long> members) throws SQLException
    {
        long groupId = nextId++;
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement("INSERT INTO groups (id, name, admin_all_projects) VALUES (?, ?, false)");
            stmt.setLong(1, groupId);
            String groupName;
            if(project.name.equals("project"))
            {
                groupName = "project admins";
            }
            else
            {
                groupName = project.name + " administrators";
            }
            stmt.setString(2, groupName);
            stmt.executeUpdate();
            JDBCUtils.close(stmt);
        }
        finally
        {
            JDBCUtils.close(stmt);
        }

        addAcl(con, project.id, groupId);

        for(Long userId: members)
        {
            addUserToGroup(con, groupId, userId);
        }
    }

    private void addAcl(Connection con, Long projectId, long groupId) throws SQLException
    {
        PreparedStatement stmt = null;

        try
        {
            stmt = con.prepareStatement("INSERT INTO project_acl_entry (id, recipient, mask, project_id) VALUES (?, ?, 4, ?)");
            stmt.setLong(1, nextId++);
            stmt.setString(2, "GROUP_" + groupId);
            stmt.setLong(3, projectId);
            stmt.executeUpdate();
            JDBCUtils.close(stmt);
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private void dropAuthorities(Connection con) throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = con.prepareStatement("DROP TABLE authorities");
            stmt.executeUpdate();
        }
        finally
        {
            JDBCUtils.close(stmt);
        }
    }

    private class UserInfo
    {
        public long id;
        public String login;
        public List<Long> projects;
        public boolean isAllProjectAdmin = false;

        public UserInfo(long id, String login)
        {
            this.id = id;
            this.login = login;
            projects = new LinkedList<Long>();
        }

    }

    private class ProjectInfo
    {
        public long id;
        public String name;

        public ProjectInfo(long id, String name)
        {
            this.id = id;
            this.name = name;
        }
    }
}
