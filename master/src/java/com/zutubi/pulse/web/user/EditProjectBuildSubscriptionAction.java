package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.*;

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
    private String conditionType;
    private List<String> selectedConditions;
    private int repeatedX = 5;
    private String repeatedUnits;
    private String expression;
    private ProjectBuildCondition condition;

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

    public ProjectBuildCondition getCondition()
    {
        return condition;
    }

    public void setCondition(ProjectBuildCondition condition)
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

    public String getConditionType()
    {
        return conditionType;
    }

    public void setConditionType(String conditionType)
    {
        this.conditionType = conditionType;
    }

    public List<String> getSelectedConditions()
    {
        return selectedConditions;
    }

    public void setSelectedConditions(List<String> selectedConditions)
    {
        this.selectedConditions = selectedConditions;
    }

    public int getRepeatedX()
    {
        return repeatedX;
    }

    public void setRepeatedX(int repeatedX)
    {
        this.repeatedX = repeatedX;
    }

    public String getRepeatedUnits()
    {
        return repeatedUnits;
    }

    public void setRepeatedUnits(String repeatedUnits)
    {
        this.repeatedUnits = repeatedUnits;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public Map getSelectedOptions()
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
        initialiseConditionFields();

        return INPUT;
    }

    public void populateProjects(ProjectBuildSubscription subscription)
    {
        for(Project p: subscription.getProjects())
        {
            projects.add(p.getId());
        }
    }

    private void initialiseConditionFields()
    {
        condition = subscription.getCondition();
        conditionType = condition.getType();
        expression = condition.getExpression();
        if(conditionType.equals("simple"))
        {
            SimpleProjectBuildCondition s = (SimpleProjectBuildCondition) condition;
            selectedConditions = s.getConditions();
        }
        else if(conditionType.equals("repeated"))
        {
            RepeatedFailuresProjectBuildCondition rf = (RepeatedFailuresProjectBuildCondition) condition;
            repeatedX = rf.getX();
            repeatedUnits = rf.getUnits();
        }
    }

    public void validate()
    {
        lookupUser();
        if(getUser() == null)
        {
            return;
        }

        lookupSubscription();
        if(subscription == null)
        {
            return;
        }

        contactPoint = subscription.getContactPoint();

        createHelper();
        helper.validateCondition(conditionType, selectedConditions, repeatedX, repeatedUnits, expression, this);

        super.validate();
    }

    public String execute()
    {
        // Create a new condition and delete the old.
        ProjectBuildCondition oldCondition = subscription.getCondition();
        condition = helper.createCondition(conditionType, selectedConditions, repeatedX, repeatedUnits, expression);
        subscription.setCondition(condition);
        getSubscriptionManager().delete(oldCondition);

        subscription.setContactPoint(contactPoint);
        subscription.setTemplate(template);
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
