package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectAclEntry;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.scheduling.Trigger;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class ConfigureProjectAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private List<Trigger> triggers;
    private UserManager userManager;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public List<Trigger> getTriggers()
    {
        return triggers;
    }

    public List<User> getProjectAdmins()
    {
        List<User> result = new LinkedList<User>();
        List<ProjectAclEntry> acls = project.getAclEntries();

        for(ProjectAclEntry acl: acls)
        {
            String recipient = (String) acl.getRecipient();
            com.zutubi.pulse.model.User user = userManager.getUser(recipient);
            if(user != null)
            {
                result.add(user);
            }
        }

        return result;
    }

    public String execute()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project '" + id + "'");
            return ERROR;
        }

        triggers = getScheduler().getTriggers(id);
        return SUCCESS;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
