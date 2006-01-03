package com.cinnamonbob.scm;

import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.model.Scm;

/**
 * This event indicates that a change has been detected in an SCM.
 */
public class SCMChangeEvent extends Event
{
    private Revision newRevision;
    private Revision previousRevision;

    public SCMChangeEvent(Scm source, Revision newRevision, Revision previousRevision)
    {
        super(source);
        this.newRevision = newRevision;
        this.previousRevision = previousRevision;
    }

    public Scm getScm()
    {
        return (Scm) getSource();
    }
    
    public Revision getNewRevision()
    {
        return newRevision;
    }

    public Revision getPreviousRevision()
    {
        return previousRevision;
    }
}