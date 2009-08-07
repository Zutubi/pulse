package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.StringUtils;

/**
 * Helper base class for actions that may drill down to the stage level.
 */
public class StageActionBase extends BuildActionBase
{
    private String stageName;
    private RecipeResultNode recipeResultNode;

    public String getStageName()
    {
        return stageName;
    }

    public void setStageName(String stageName)
    {
        this.stageName = stageName;
    }

    public String getu_stageName()
    {
        return uriComponentEncode(stageName);
    }

    public String geth_stageName()
    {
        return htmlEncode(stageName);
    }

    public RecipeResultNode getRecipeResultNode()
    {
        BuildResult buildResult = getBuildResult();
        if (recipeResultNode == null && buildResult != null)
        {
            if (StringUtils.stringSet(stageName))
            {
                recipeResultNode = buildResult.findResultNode(stageName);
                if(recipeResultNode == null)
                {
                    throw new LookupErrorException("Unknown stage [" + stageName + "] for build [" + buildResult.getNumber() + "] of project [" + buildResult.getProject().getName() + "]");
                }
            }
        }

        return recipeResultNode;
    }

    public RecipeResultNode getRequiredRecipeResultNode()
    {
        RecipeResultNode node = getRecipeResultNode();
        if(node == null)
        {
            throw new LookupErrorException("Stage name is required");
        }

        return node;
    }
}
