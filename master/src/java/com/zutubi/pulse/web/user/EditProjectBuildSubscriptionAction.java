package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectBuildSubscription;
import com.zutubi.pulse.model.Subscription;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class EditProjectBuildSubscriptionAction extends SubscriptionActionSupport
{
    private long id;
    private ProjectBuildSubscription subscription;
    private List<Long> projects = new LinkedList<Long>();
    private String template;
    private String condition;

    public EditProjectBuildSubscriptionAction()
    {
        super(false);
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public List<Long> getProjects()
    {
        return projects;
    }

    public void setProjects(List<Long> projects)
    {
        this.projects = projects;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public Map<String, String> getAvailableTemplates()
    {
        return helper.getAvailableTemplates();
    }

    public Map getConditions()
    {
        return helper.getConditions();
    }

    public Map<Long, String> getAllProjects()
    {
        return helper.getAllProjects();
    }

    public String doInput() throws Exception
    {
        super.doInput();
        if(hasErrors())
        {
            return ERROR;
        }

        populateProjects(subscription);
        contactPointId = subscription.getContactPoint().getId();
        template = subscription.getTemplate();
        condition = subscription.getCondition();

        return INPUT;
    }

    public void populateProjects(ProjectBuildSubscription subscription)
    {
        for(Project p: subscription.getProjects())
        {
            projects.add(p.getId());
        }
    }

    public void validate()
    {
        lookupUser();
        if(hasErrors())
        {
            return;
        }

        lookupSubscription();
        if(hasErrors())
        {
            return;
        }

        contactPoint = subscription.getContactPoint();
        createHelper();
        helper.validateCondition(condition, this);

        super.validate();
    }

    public String execute()
    {
        subscription.setContactPoint(contactPoint);
        subscription.setTemplate(template);
        subscription.setCondition(condition);
        helper.setProjects(projects);
        helper.updateProjects(subscription);

        getSubscriptionManager().save(subscription);
        return SUCCESS;
    }

    protected Subscription lookupSubscription()
    {
        Subscription s = user.getSubscription(id);
        if(s == null || !(s instanceof ProjectBuildSubscription))
        {
            addActionError("Unknown subscription [" + id + "]");
            return null;
        }
        else
        {
            subscription = (ProjectBuildSubscription) s;
            return s;
        }
    }
}
