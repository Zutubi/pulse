package com.cinnamonbob.web.project;

import com.cinnamonbob.model.*;

import java.util.Iterator;
import java.util.List;

/**
 * 
 *
 */
public class ViewBuildAction extends ProjectActionSupport
{
    private long id;
    private long buildId;
    private Project project;
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

    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long id)
    {
        this.buildId = id;
    }

    public Project getProject()
    {
        return project;
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
        project = getProjectManager().getProject(id);
        result = getBuildManager().getBuildResult(buildId);

        for(CommandResult r: result.getCommandResults())
        {
            for(StoredArtifact a: r.getArtifacts())
            {
                Iterator<Feature.Level> i = a.getLevels();
                while(i.hasNext())
                {
                    a.getFeatures(i.next()).size();
                }
            }
        }

        //changes = result.getChangelists();

        return SUCCESS;
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
