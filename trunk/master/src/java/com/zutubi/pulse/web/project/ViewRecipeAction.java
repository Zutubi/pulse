package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.model.RecipeResultNode;

/**
 * 
 *
 */
public class ViewRecipeAction extends ProjectActionSupport
{
    private long id;
    private RecipeResultNode node;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public RecipeResultNode getNode()
    {
        return node;
    }

    public void validate()
    {
        node = getBuildManager().getRecipeResultNode(id);
        if (node == null)
        {
            addActionError("Unknown recipe node '" + id + "'");
        }
    }

    public String execute()
    {
        for (CommandResult r : node.getResult().getCommandResults())
        {
            for (StoredArtifact a : r.getArtifacts())
            {
                Iterable<Feature.Level> i = a.getLevels();
                for (Feature.Level level : i)
                {
                    a.getFeatures(level).size();
                }
            }
        }

        return SUCCESS;
    }
}
