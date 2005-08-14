package com.cinnamonbob.web.project;

import java.util.Iterator;
import java.util.List;

import com.cinnamonbob.scm.Changelist;
import com.cinnamonbob.model.NumericalRevision;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.core.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.CommandResult;
import com.cinnamonbob.model.StoredArtifact;
import com.cinnamonbob.model.Feature;

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
        
        if(result.getNumber() > 1)
        {
            BuildResult previousResult = getBuildManager().getByProjectNameAndNumber(project.getName(), result.getNumber() - 1);
            
            if(previousResult != null)
            {
                getChanges(previousResult);
            }
        }

        return SUCCESS;
    }

    private void getChanges(BuildResult previousResult)
    {
        // FIXME oh how it assumes...
        NumericalRevision previousRevision = new NumericalRevision(Long.parseLong(previousResult.getRevision()));
        NumericalRevision buildRevision    = new NumericalRevision(Long.parseLong(result.getRevision()));
        
        try
        {
            changes = project.getScms().get(0).createServer().getChanges(previousRevision, buildRevision, "");
        }
        catch(SCMException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
