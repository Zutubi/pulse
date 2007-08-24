package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildResult;

/**
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

        projectManager.checkWrite(result.getProject());
        buildManager.delete(result);
        return SUCCESS;
    }
}
