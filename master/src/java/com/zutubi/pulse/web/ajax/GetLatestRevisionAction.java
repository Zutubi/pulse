package com.zutubi.pulse.web.ajax;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.web.project.ProjectActionSupport;

/**
 * Simple ajax action to retrieve the latest revision for a project, used on
 * the build properties editing page (prompt on trigger).
 */
public class GetLatestRevisionAction extends ProjectActionSupport
{
    private boolean successful = false;
    private String latestRevision;
    private String error;

    public boolean isSuccessful()
    {
        return successful;
    }

    public String getLatestRevision()
    {
        return latestRevision;
    }

    public String getError()
    {
        return error;
    }

    public String execute()
    {
        Project project = getProject();
        if(project == null)
        {
            error = "Unknown project";
        }
        else
        {
            try
            {
                latestRevision = project.getScm().createServer().getLatestRevision().getRevisionString();
                successful = true;
            }
            catch (Exception e)
            {
                error = e.toString();
            }

        }
        
        return SUCCESS;
    }
}
