package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.Feature;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.model.RecipeResultNode;

import java.util.Iterator;

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
                Iterator<Feature.Level> i = a.getLevels();
                while (i.hasNext())
                {
                    a.getFeatures(i.next()).size();
                }
            }
        }

        return SUCCESS;
    }
}
