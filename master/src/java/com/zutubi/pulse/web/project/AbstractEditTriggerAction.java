/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.BuildProjectTask;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public abstract class AbstractEditTriggerAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private Project project;
    private String specification;
    private List<String> specifications;
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

        specifications = new LinkedList<String>();
        for (BuildSpecification spec : project.getBuildSpecifications())
        {
            specifications.add(spec.getName());
        }

        Trigger trigger = getTrigger();
        long buildSpecId = (Long)trigger.getDataMap().get(BuildProjectTask.PARAM_SPEC);
        specification = getProjectManager().getBuildSpecification(buildSpecId).getName();
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
            BuildSpecification buildSpec = getProjectManager().getBuildSpecification(specification);

            Scheduler scheduler = getScheduler();

            Trigger trigger = getTrigger();
            trigger.getDataMap().put(BuildProjectTask.PARAM_SPEC, buildSpec.getId());
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
