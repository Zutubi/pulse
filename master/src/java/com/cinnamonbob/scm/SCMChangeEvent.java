package com.cinnamonbob.scm;

import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.model.Scm;

/**
 * This event indicates that a change has been detected in an SCM.
 */
public class SCMChangeEvent extends Event
{
    private Revision revision;

    public SCMChangeEvent(Scm source, Revision revision)
    {
        super(source);
        this.revision = revision;
    }

    public Scm getScm()
    {
        return (Scm) getSource();
    }
    
    public Revision getRevision()
    {
        return revision;
    }
}