package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Scm;
import com.cinnamonbob.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;

/**
 *
 *
 */
public abstract class AbstractEditScmAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private long projectId;
    private Project project;
    private static final List<String> ID_PARAMS = Arrays.asList("id");

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

    public List<String> getPrepareParameterNames()
    {
        return ID_PARAMS;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);
        return INPUT;
    }

    public String execute()
    {
        getScmManager().save(getScm());
        return SUCCESS;
    }

    public abstract Scm getScm();

    public abstract String getScmProperty();

}
