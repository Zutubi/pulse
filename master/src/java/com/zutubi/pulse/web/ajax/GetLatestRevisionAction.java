package com.zutubi.pulse.web.ajax;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.ScmCapability;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.master.scm.ScmContextFactory;
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
    private ScmClientFactory scmClientFactory;
    private ScmContextFactory scmContextFactory;

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
                ScmContext context = scmContextFactory.createContext(project.getConfig().getProjectId(), project.getConfig().getScm());
                client = scmClientFactory.createClient(project.getConfig().getScm());
                if(client.getCapabilities().contains(ScmCapability.REVISIONS))
                {
                    latestRevision = client.getLatestRevision(context).getRevisionString();
                }
                else
                {
                    latestRevision = new Revision(System.currentTimeMillis()).getRevisionString();
                }

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

    public void setScmContextFactory(ScmContextFactory scmContextFactory)
    {
        this.scmContextFactory = scmContextFactory;
    }
}
