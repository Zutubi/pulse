/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.CustomPulseFileDetails;
import com.zutubi.pulse.model.Project;

/**
 * 
 *
 */
public class CreateProjectAction extends ProjectActionSupport
{
    private Project project = new Project();
    private String pulseFileName = "pulse.xml";

    public Project getProject()
    {
        return project;
    }

    public long getId()
    {
        return getProject().getId();
    }

    public String getPulseFileName()
    {
        return pulseFileName;
    }

    public void setPulseFileName(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
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
        project.setPulseFileDetails(new CustomPulseFileDetails(pulseFileName));
        getProjectManager().save(project);
        return SUCCESS;
    }
}
