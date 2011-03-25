package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.User;
import com.zutubi.util.StringUtils;

/**
 * Action for the build artifacts tab.
 */
public class ViewArtifactsAction extends CommandActionBase
{
    private String artifactName;
    private String filter = User.DEFAULT_ARTIFACTS_FILTER;

    public void setArtifactName(String artifactName)
    {
        this.artifactName = artifactName;
    }

    public String getFilter()
    {
        return filter;
    }

    public long getSelectedId()
    {
        long id = 0;

        RecipeResultNode recipeResultNode = getRecipeResultNode();
        if (recipeResultNode != null)
        {
            CommandResult commandResult = getCommandResult();
            if (commandResult != null)
            {
                if (StringUtils.stringSet(artifactName) && commandResult.getArtifact(artifactName) != null)
                {
                    id = commandResult.getArtifact(artifactName).getId();
                }
                else
                {
                    id = commandResult.getId();
                }
            }
            else
            {
                id = recipeResultNode.getResult().getId();
            }
            
        }
        
        return id;
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
