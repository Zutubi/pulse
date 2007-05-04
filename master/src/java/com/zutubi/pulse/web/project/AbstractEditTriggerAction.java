package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class AbstractEditTriggerAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private Project project;
    private long specification;
    private Map<Long, String> specifications;
    private static final List<String> ID_PARAMS = Arrays.asList("id", "projectId");

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

    public Map<Long, String> getSpecifications()
    {
        return specifications;
    }

    public long getSpecification()
    {
        return specification;
    }

    public void setSpecification(long specification)
    {
        this.specification = specification;
    }

    public List<String> getPrepareParameterNames()
    {
        return ID_PARAMS;
    }

    public void prepare() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

/*
        specifications = new LinkedHashMap<Long, String>();
        for (BuildSpecification spec : project.getBuildSpecifications())
        {
            specifications.put(spec.getId(), spec.getName());
        }

        Trigger trigger = getTrigger();
        specification = (Long) trigger.getDataMap().get(BuildProjectTask.PARAM_SPEC);
*/
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
