package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Collections;
import java.util.List;

/**
 */
public class GroupsActionSupport extends ActionSupport
{
    private UserManager userManager;
    private ProjectManager projectManager;

    public UserManager getUserManager()
    {
        return userManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public boolean hasAdminAuthority(Group group)
    {
        return group.hasAuthority(GrantedAuthority.ADMINISTRATOR);
    }

    public boolean hasPersonalAuthority(Group group)
    {
        return group.hasAuthority(GrantedAuthority.PERSONAL);
    }

    public List<Project> getAdminProjects(Group group)
    {
        return projectManager.getProjectsWithAdmin(group.getDefaultAuthority());
    }

    protected int getGroupStartPage(Group group)
    {
        List<Group> all = getUserManager().getAllGroups();
        Collections.sort(all, new NamedEntityComparator());
        int index = all.indexOf(group);
        return index / ViewGroupsAction.GROUPS_PER_PAGE;
    }
}
