package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.security.AccessManager;

/**
 * Used to manually delete a build result from the web UI.
 */
public class DeleteBuildAction extends BuildActionBase
{
    public String execute()
    {
        BuildResult result = getRequiredBuildResult();
        if(!result.completed())
        {
            addActionError("Build cannot be deleted as it is not complete.");
            return ERROR;
        }

        accessManager.ensurePermission(AccessManager.ACTION_WRITE, result.getProject());
        buildManager.delete(result);

        if(isPersonal())
        {
            return "personal";
        }
        else
        {
            return SUCCESS;
        }
    }
}
