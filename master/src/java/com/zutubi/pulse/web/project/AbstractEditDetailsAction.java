package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BobFileDetails;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.BobFileDetailsDao;
import com.zutubi.pulse.xwork.interceptor.Preparable;

import java.util.Arrays;
import java.util.List;

/**
 *
 *
 */
public abstract class AbstractEditDetailsAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private long projectId;
    private Project project;
    private BobFileDetailsDao bobFileDetailsDao;
    private static final List<String> PREPARE_PARAMS = Arrays.asList("id", "projectId");

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

    public BobFileDetailsDao getBobFileDetailsDao()
    {
        return bobFileDetailsDao;
    }

    public void setBobFileDetailsDao(BobFileDetailsDao bobFileDetailsDao)
    {
        this.bobFileDetailsDao = bobFileDetailsDao;
    }

    public List<String> getPrepareParameterNames()
    {
        return PREPARE_PARAMS;
    }

    public String doInput() throws Exception
    {
        prepare();

        project = getProjectManager().getProject(projectId);

        if (getDetails() == null)
        {
            addActionError("Unknown project details '" + getId() + "'");
            return ERROR;
        }

        return INPUT;
    }

    public String execute()
    {
        if (getDetails() == null)
        {
            return INPUT;
        }

        getBobFileDetailsDao().save(getDetails());
        return SUCCESS;
    }

    public abstract BobFileDetails getDetails();
}
