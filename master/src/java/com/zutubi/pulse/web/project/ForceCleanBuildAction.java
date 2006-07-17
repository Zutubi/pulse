package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;

public class ForceCleanBuildAction extends ProjectActionSupport
{
    private long id;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void validate()
    {
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(projectId);

        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        getProjectManager().checkWrite(project);

        BuildSpecification spec = project.getBuildSpecification(id);
        if (spec == null)
        {
            addActionError("Unknown build specification [" + id + "]");
            return ERROR;
        }

        spec.setForceClean(true);
        getProjectManager().save(spec);

        return SUCCESS;
    }
}
