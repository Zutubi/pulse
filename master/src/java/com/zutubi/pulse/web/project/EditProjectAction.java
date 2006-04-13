/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;

/**
 * 
 *
 */
public class EditProjectAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private Project project;// = new Project();

    private static final List<String> ID_PARAMS = Arrays.asList("id");

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public Project getProject()
    {
        return project;
    }

    public boolean checkProject()
    {
        if (project == null)
        {
            addActionError("Unknown project [" + getId() + "]");
            return true;
        }

        return false;
    }

    public List<String> getPrepareParameterNames()
    {
        return ID_PARAMS;
    }

    public void prepare() throws Exception
    {
        project = getProjectManager().getProject(getId());
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        if (checkProject())
        {
            return;
        }

        // check that the requested projects name is not already in use.
        Project existingProject = getProjectManager().getProject(project.getName());
        if (existingProject != null && getId() != existingProject.getId())
        {
            addFieldError("project.name", getText("project.name.exists", Arrays.asList(new String[]{project.getName()})));
        }
    }

    public String doInput()
    {
        if (checkProject())
        {
            return ERROR;
        }

        return INPUT;
    }

    public String execute()
    {
        getProjectManager().save(project);
        return SUCCESS;
    }

}
