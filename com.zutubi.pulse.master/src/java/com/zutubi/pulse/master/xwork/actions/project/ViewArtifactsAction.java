package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.User;

/**
 */
public class ViewArtifactsAction extends CommandActionBase
{
    private String filter = User.DEFAULT_ARTIFACTS_FILTER;

    public String getFilter()
    {
        return filter;
    }

    public String execute()
    {
        // Optional discovery down to the command level.
        getCommandResult();
        // We require at least down to the build level
        getRequiredBuildResult();

        User user = getLoggedInUser();
        if (user != null)
        {
            filter = user.getArtifactsFilter();
        }
        
        return SUCCESS;
    }
}
