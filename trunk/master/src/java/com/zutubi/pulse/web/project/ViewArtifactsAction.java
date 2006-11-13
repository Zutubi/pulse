package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;

/**
 *
 *
 */
public class ViewArtifactsAction extends ProjectActionSupport
{
    private long id;
    private BuildResult result;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return result.getProject();
    }

    public BuildResult getResult()
    {
        return result;
    }

    public String execute()
    {
        result = getBuildManager().getBuildResult(id);

        if (result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        return SUCCESS;
    }
}
