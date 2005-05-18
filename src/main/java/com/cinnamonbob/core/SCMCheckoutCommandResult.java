package com.cinnamonbob.core;

import java.util.List;

import com.cinnamonbob.core.scm.Changelist;
import com.cinnamonbob.core.scm.Revision;
import com.cinnamonbob.core.scm.SCMException;


public class SCMCheckoutCommandResult implements CommandResult
{
    private Revision revision;
    private List<Changelist> changes;
    private SCMException exception;
    
    
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
        for(Changelist list: changes)
        {
            if(list.getUser().equals(login))
            {
                return true;
            }
        }
        
        System.out.println("not changed by " + login);
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
}
