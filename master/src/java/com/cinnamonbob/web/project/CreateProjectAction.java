package com.cinnamonbob.web.project;

import com.cinnamonbob.model.*;
import com.cinnamonbob.model.persistence.SlaveDao;

/**
 * 
 *
 */
public class CreateProjectAction extends ProjectActionSupport
{
    private Project project = new Project();
    private SlaveDao slaveDao;

    public Project getProject()
    {
        return project;
    }

    public long getId()
    {
        return getProject().getId();
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

        if (getProjectManager().getProject(project.getName()) != null)
        {
            // login name already in use.
            addFieldError("project.name", "Project name " + project.getName() + " is already being used.");
        }
    }

    public String execute()
    {
        // TODO: remove once we have a GUI to add this stuff
        Slave slave = slaveDao.findAll().get(0);
        BuildSpecification spec = new BuildSpecification("default");
        spec.getRoot().addChild(new BuildSpecificationNode(new SlaveBuildHostRequirements(slave), null));
        project.addBuildSpecification(spec);
        getProjectManager().save(project);
        return SUCCESS;
    }

    public void setSlaveDao(SlaveDao slaveDao)
    {
        this.slaveDao = slaveDao;
    }
}
