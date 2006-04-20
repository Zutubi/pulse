/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Scm;

/**
 * This event indicates that a change has been detected in an SCM.
 */
public class SCMChangeEvent extends Event<Scm>
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
        return getSource();
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