package com.zutubi.pulse.web.ajax;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
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
    private ScmClientFactory scmClientFactory;

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
            ScmClient client = null;
            try
            {
                client = scmClientFactory.createClient(project.getConfig().getScm());
                latestRevision = client.getLatestRevision().getRevisionString();
                successful = true;
            }
            catch (Exception e)
            {
                error = e.toString();
            }
            finally
            {
                ScmClientUtils.close(client);
            }
        }
        
        return SUCCESS;
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
