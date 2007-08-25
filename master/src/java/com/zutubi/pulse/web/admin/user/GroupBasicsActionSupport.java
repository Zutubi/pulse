package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

import java.util.*;

/**
 */
public class GroupBasicsActionSupport extends GroupActionSupport
{
    private boolean admin;
    private boolean personal;
    private boolean adminAllProjects;
    private Map<Long, String> allProjects;
    private List<Long> projects = new ArrayList<Long>();
    private int startPage = 0;

    public boolean isAdmin()
    {
        return admin;
    }

    public void setAdmin(boolean admin)
    {
        this.admin = admin;
    }

    public boolean isPersonal()
    {
        return personal;
    }

    public void setPersonal(boolean personal)
    {
        this.personal = personal;
    }

    public boolean isAdminAllProjects()
    {
        return adminAllProjects;
    }

    public void setAdminAllProjects(boolean adminAllProjects)
    {
        this.adminAllProjects = adminAllProjects;
    }

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public Map<Long, String> getAllProjects()
    {
        if(allProjects == null)
        {
            // FIXME: sort the map.
            allProjects = new LinkedHashMap<Long, String>();
            Collection<ProjectConfiguration> all = projectManager.getAllProjectConfigs(true);
            for(ProjectConfiguration p: all)
            {
                allProjects.put(p.getHandle(), p.getName());
            }
        }
        return allProjects;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
    }


    protected void setPermissions(Group group)
    {
        if(isAdmin())
        {
            group.addAdditionalAuthority(GrantedAuthority.ADMINISTRATOR);
        }
        else
        {
            group.removeAdditionalAuthority(GrantedAuthority.ADMINISTRATOR);
        }

        if(isPersonal())
        {
            group.addAdditionalAuthority(GrantedAuthority.PERSONAL);
        }
        else
        {
            group.removeAdditionalAuthority(GrantedAuthority.PERSONAL);
        }

        group.setAdminAllProjects(isAdminAllProjects());

        getUserManager().save(group);
        List<Long> projects = null;
        if(!group.getAdminAllProjects())
        {
            projects = getProjects();
        }
        getProjectManager().updateProjectAdmins(group.getDefaultAuthority(), projects);
    }
}
