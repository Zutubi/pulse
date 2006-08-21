package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.web.user.UserActionSupport;
import com.zutubi.pulse.model.*;

import java.util.*;

/**
 *
 *
 */
public class EditGroupAction extends GroupBasicsActionSupport
{
    private String newName;

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
    }

    public String doInput()
    {
        Group group = getGroup();
        if(group == null)
        {
            addActionError("Unknown group [" + getGroupId() + "]");
            return ERROR;
        }

        setStartPage(getGroupStartPage(group));
        newName = group.getName();
        setAdmin(hasAdminAuthority(group));
        setAdminAllProjects(group.getAdminAllProjects());

        List<Project> adminProjects = getProjectManager().getProjectsWithAdmin(group.getDefaultAuthority());
        List<Long> projects = new ArrayList<Long>(adminProjects.size());
        for(Project p: adminProjects)
        {
            projects.add(p.getId());
        }
        setProjects(projects);

        return INPUT;
    }

    public void validate()
    {
        if(hasErrors())
        {
            return;
        }
        
        if(getGroup() == null)
        {
            addActionError("Unknown group [" + getGroupId() + "]");
            return;
        }

        if(!newName.equals(getGroup().getName()) && getUserManager().getGroup(newName) != null)
        {
            addFieldError("newName", getText("group.name.duplicate", Arrays.asList(new Object[] { newName })));
        }
    }

    public String execute()
    {
        Group group = getGroup();
        group.setName(newName);
        setPermissions(group);
        setStartPage(getGroupStartPage(group));
        return SUCCESS;
    }
}
