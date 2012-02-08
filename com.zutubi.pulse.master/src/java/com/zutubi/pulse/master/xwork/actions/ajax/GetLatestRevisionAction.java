package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.xwork.actions.project.ProjectActionSupport;
import com.zutubi.util.TimeStamps;

import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;

/**
 * Simple ajax action to retrieve the latest revision for a project, used on
 * the build properties editing page (prompt on trigger).
 */
public class GetLatestRevisionAction extends ProjectActionSupport
{
    private ScmManager scmManager;

    private GetLatestRevisionActionResult result;

    public GetLatestRevisionActionResult getResult()
    {
        return result;
    }

    public String execute()
    {
        result = new GetLatestRevisionActionResult();

        final Project project = getProject();
        if(project == null)
        {
            result.setError("Unknown project");
        }
        else
        {
            try
            {
                result.setLatestRevision(withScmClient(project.getConfig(), project.getState(), scmManager, new ScmContextualAction<String>()
                {
                    public String process(ScmClient client, ScmContext context) throws ScmException
                    {
                        if(client.getCapabilities(context).contains(ScmCapability.REVISIONS))
                        {
                            return client.getLatestRevision(context).getRevisionString();
                        }
                        else
                        {
                            return new Revision(TimeStamps.getPrettyDate(System.currentTimeMillis(), getLocale())).getRevisionString();
                        }
                    }
                }));

                result.setSuccessful(true);
            }
            catch (Exception e)
            {
                result.setError(e.toString());
            }
        }
        
        return SUCCESS;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
