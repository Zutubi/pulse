package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmCapability;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.xwork.actions.project.ProjectActionSupport;
import com.zutubi.util.TimeStamps;

/**
 * Simple ajax action to retrieve the latest revision for a project, used on
 * the build properties editing page (prompt on trigger).
 */
public class GetLatestRevisionAction extends ProjectActionSupport
{
    private boolean successful = false;
    private String latestRevision;
    private String error;
    private ScmManager scmManager;

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
            ScmConfiguration scm = project.getConfig().getScm();
            ScmClient client = null;
            try
            {
                ScmContext context = scmManager.createContext(project.getConfig().getProjectId(), project.getConfig().getScm());
                client = scmManager.createClient(scm);
                if(client.getCapabilities().contains(ScmCapability.REVISIONS))
                {
                    latestRevision = client.getLatestRevision(context).getRevisionString();
                }
                else
                {
                    latestRevision = new Revision(TimeStamps.getPrettyDate(System.currentTimeMillis(), getLocale())).getRevisionString();
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

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
