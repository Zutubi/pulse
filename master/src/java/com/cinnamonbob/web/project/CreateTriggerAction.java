package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.scheduling.CronTrigger;
import com.cinnamonbob.scheduling.SchedulingException;
import com.cinnamonbob.scheduling.tasks.BuildProjectTask;
import com.cinnamonbob.util.logging.Logger;

/**
 *
 *
 */
public class CreateTriggerAction extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(CreateTriggerAction.class.getName());

    private long project;
    private String name;
    private String recipe;
    private String cronExpression;

    public long getProject()
    {
        return project;
    }

    public void setProject(long project)
    {
        this.project = project;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        // ensure the project id is valid.
        Project project = getProjectManager().getProject(this.project);
        if (project == null)
        {
            addActionError("No project with id '" + Long.toString(this.project) + "'");
            return;
        }

        Trigger trigger = getScheduler().getTrigger(project.getId(), name);
        // ensure that the name is unique to the project.
        if (trigger != null)
        {
            addFieldError("name", "Name already within this project.");
        }
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(getProject());

        CronTrigger trigger = new CronTrigger(cronExpression, name);
        trigger.setProject(project.getId());
        trigger.setTaskClass(BuildProjectTask.class);

        try
        {
            getScheduler().schedule(trigger);
            return SUCCESS;
        }
        catch (SchedulingException e)
        {
            LOG.severe(e.getMessage(), e);
            return ERROR;
        }

    }

    public String doDefault()
    {
        return SUCCESS;
    }
}
