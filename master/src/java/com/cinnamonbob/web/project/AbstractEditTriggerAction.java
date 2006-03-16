package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.Scheduler;
import com.cinnamonbob.scheduling.SchedulingException;
import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.scheduling.tasks.BuildProjectTask;
import com.opensymphony.xwork.Preparable;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public abstract class AbstractEditTriggerAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private long projectId;
    private Project project;
    private String specification;
    private List<String> specifications;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return project;
    }

    public List<String> getSpecifications()
    {
        return specifications;
    }

    public String getSpecification()
    {
        return specification;
    }

    public void setSpecification(String specification)
    {
        this.specification = specification;
    }

    public void prepare() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        specifications = new LinkedList<String>();
        for (BuildSpecification spec : project.getBuildSpecifications())
        {
            specifications.add(spec.getName());
        }

        Trigger trigger = getTrigger();
        specification = (String) trigger.getDataMap().get(BuildProjectTask.PARAM_SPEC);
    }

    public String doInput()
    {
        if (hasErrors())
        {
            return ERROR;
        }

        return INPUT;
    }

    public String execute()
    {
        if (hasErrors())
        {
            return ERROR;
        }

        try
        {
            Scheduler scheduler = getScheduler();

            Trigger trigger = getTrigger();
            trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, specification);
            scheduler.update(trigger);

            return SUCCESS;
        }
        catch (SchedulingException e)
        {
            addActionError("Unable to update trigger: " + e.getMessage());
            return ERROR;
        }
    }

    public abstract Trigger getTrigger();
}
