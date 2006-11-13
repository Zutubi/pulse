package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;

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

    private List<CommitMessageTransformer> transformers;

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

    public int getDefaultScmPollingInterval()
    {
        return scmManager.getDefaultPollingInterval();
    }

    public List<CommitMessageTransformer> getTransformers()
    {
        return transformers;
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

    public String getSpec(Trigger trigger)
    {
        Long id = (Long) trigger.getDataMap().get(BuildProjectTask.PARAM_SPEC);
        if(id != null)
        {
            BuildSpecification spec = project.getBuildSpecification(id);
            if (spec != null)
            {
                return spec.getName();
            }
        }

        return "";
    }

    public boolean isDefault(BuildSpecification specification)
    {
        return project.getDefaultSpecification().equals(specification);
    }

    public void validate()
    {
        if(id != 0)
        {
            projectId = id;
        }

        project = super.getProject();
        if(project == null)
        {
            addUnknownProjectActionError();
        }
    }

    public String execute()
    {
        if(id != 0)
        {
            projectId = id;
        }
        
        project = super.getProject();
        id = project.getId();
        triggers = getScheduler().getTriggers(id);

        transformers = transformerManager.getByProject(project);

        return SUCCESS;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
