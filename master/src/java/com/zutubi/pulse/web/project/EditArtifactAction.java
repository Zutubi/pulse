/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.Capture;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.TemplatePulseFileDetails;
import com.zutubi.pulse.scheduling.Trigger;

/**
 *
 */
public class EditArtifactAction extends ProjectActionSupport
{
    private long id;
    private long projectId;
    private Project project;
    private Capture capture;

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

    public Capture getCapture()
    {
        return capture;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        PulseFileDetails details = project.getPulseFileDetails();
        if(!details.isBuiltIn())
        {
            addActionError("Icalid operation for project");
            return ERROR;
        }

        TemplatePulseFileDetails template = (TemplatePulseFileDetails) details;
        capture = template.getCapture(id);
        if (capture == null)
        {
            addActionError("Unknown artifact [" + id + "]");
            return ERROR;
        }

        return capture.getType();
    }
}
