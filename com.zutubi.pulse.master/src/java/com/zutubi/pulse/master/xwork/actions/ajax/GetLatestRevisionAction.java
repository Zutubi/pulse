package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.model.Project;
import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;
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
        final Project project = getProject();
        if(project == null)
        {
            error = "Unknown project";
        }
        else
        {
            try
            {
                latestRevision = withScmClient(project.getConfig(), scmManager, new ScmContextualAction<String>()
                {
                    public String process(ScmClient client, ScmContext context) throws ScmException
                    {
                        ScmContext c = (project.isInitialised()) ? context : null;
                        if(client.getCapabilities(c).contains(ScmCapability.REVISIONS))
                        {
                            return client.getLatestRevision(context).getRevisionString();
                        }
                        else
                        {
                            return new Revision(TimeStamps.getPrettyDate(System.currentTimeMillis(), getLocale())).getRevisionString();
                        }
                    }
                });

                successful = true;
            }
            catch (Exception e)
            {
                error = e.toString();
            }
        }
        
        return SUCCESS;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
