package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RecipeResultNode;

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
    private long selectedNode;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getSelectedNode()
    {
        return selectedNode;
    }

    public void setSelectedNode(long selectedNode)
    {
        this.selectedNode = selectedNode;
    }

    public boolean haveSelectedNode()
    {
        return selectedNode != 0L && selectedNode != result.getId();
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
