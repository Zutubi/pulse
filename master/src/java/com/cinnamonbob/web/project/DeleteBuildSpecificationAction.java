package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.persistence.BuildSpecificationDao;
import com.cinnamonbob.web.ActionSupport;

/**
 *
 *
 */
public class DeleteBuildSpecificationAction extends ActionSupport
{
    private long id;
    private long projectId;
    private BuildSpecification specification;
    private BuildSpecificationDao buildSpecificationDao;
    private ProjectManager projectManager;

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

    public void validate()
    {
        specification = buildSpecificationDao.findById(id);
        if (specification == null)
        {
            addActionError("Unknown specification [" + id + "]");
        }
    }

    public String execute()
    {
        projectManager.deleteBuildSpecification(projectId, id);
        return SUCCESS;
    }

    public void setBuildSpecificationDao(BuildSpecificationDao buildSpecificationDao)
    {
        this.buildSpecificationDao = buildSpecificationDao;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
