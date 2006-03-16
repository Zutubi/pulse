package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.Feature;
import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.RecipeResultNode;

import java.util.List;

/**
 * 
 *
 */
public class ViewBuildAction extends ProjectActionSupport
{
    private long id;
    private BuildResult result;
    private List<Changelist> changes;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return result.getProject();
    }

    public BuildResult getResult()
    {
        return result;
    }

    public void validate()
    {

    }

    public String execute()
    {
        result = getBuildManager().getBuildResult(id);
        if (result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        scrapeNode(result.getRoot());
        return SUCCESS;
    }

    private void scrapeNode(RecipeResultNode parent)
    {
        for (RecipeResultNode node : parent.getChildren())
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

            scrapeNode(node);
        }
    }

    public List<Changelist> getChanges()
    {
        return changes;
    }

    public void setChanges(List<Changelist> changes)
    {
        this.changes = changes;
    }
}
