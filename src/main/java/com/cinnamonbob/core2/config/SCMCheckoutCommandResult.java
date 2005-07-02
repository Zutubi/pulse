package com.cinnamonbob.core2.config;

import com.cinnamonbob.scm.Changelist;
import com.cinnamonbob.scm.Revision;
import com.cinnamonbob.scm.SCMException;

import java.util.ArrayList;
import java.util.List;


public class SCMCheckoutCommandResult implements CommandResult
{
    private Revision revision;
    private List<Changelist> changes;
    private SCMException exception;    
    private List<Artifact> artifacts = new ArrayList<Artifact>();
    
    public SCMCheckoutCommandResult(Revision revision, List<Changelist> changes)
    {
        this.revision  = revision;
        this.changes   = changes;
        this.exception = null;
    }
    
    public SCMCheckoutCommandResult(SCMException exception)
    {
        this.revision  = null;
        this.changes   = null;
        this.exception = exception;
    }
    
    public boolean succeeded()
    {
        return exception == null;
    }

    public String getSummary()
    {
        if(exception == null)
        {
            return "Checked out revision " + revision;
        }
        else
        {
            return exception.toString();
        }
    }
    
    /**
     * @see com.cinnamonbob.core.CommandResult#changedBy(java.lang.String)
     */
    public boolean changedBy(String login)
    {
        // Double-check as changes will not be there on failure
        if(changes != null)
        {
            for(Changelist list: changes)
            {
                if(list.getUser().equals(login))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public Revision getRevision()
    {
        return revision;
    }

    public List<Changelist> getChanges()
    {
        return changes;
    }
    
    public SCMException getException()
    {
        return exception;
    }

    public List<Artifact> getArtifacts()
    {
        return artifacts;  
    }
    
    void add(Artifact a)
    {
        artifacts.add(a);
    }
}
