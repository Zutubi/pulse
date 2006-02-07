package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BobFileDetails;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.persistence.BobFileDetailsDao;
import com.opensymphony.xwork.Preparable;

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
