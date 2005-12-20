package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.web.ActionSupport;

/**
 */
public class CreateBuildSpecificationAction extends ActionSupport
{
    private long projectId;
    private Project project;
    private BuildSpecification specification = new BuildSpecification();
    private ProjectDao projectDao;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public BuildSpecification getSpecification()
    {
        return specification;
    }

    public void setSpecification(BuildSpecification specification)
    {
        this.specification = specification;
    }

    public void setProjectDao(ProjectDao projectDao)
    {
        this.projectDao = projectDao;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have
            // completed successfully.
            return;
        }

        project = projectDao.findById(projectId);
        if (project == null)
        {
            addActionError("No project found for id '" + projectId + "'");
        }

        if (project.getBuildSpecification(specification.getName()) != null)
        {
            addFieldError("specification.name", "A build specification with name '" + specification.getName() + "' already exists in this project.");
        }
    }

    public String execute()
    {
        project.addBuildSpecification(specification);
        projectDao.save(project);
        return SUCCESS;
    }

    public String doDefault()
    {
        // setup any default data.
        return SUCCESS;
    }
}
