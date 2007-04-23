package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;

/**
 * This event indicates that a change has been detected in an SCM.
 */
public class ScmChangeEvent extends Event<ProjectConfiguration>
{
    private Revision newRevision;
    private Revision previousRevision;

    public ScmChangeEvent(ProjectConfiguration source, Revision newRevision, Revision previousRevision)
    {
        super(source);
        this.newRevision = newRevision;
        this.previousRevision = previousRevision;
    }

    public Revision getNewRevision()
    {
        return newRevision;
    }

    public Revision getPreviousRevision()
    {
        return previousRevision;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("SCM Change Event");
        if (getSource() != null)
        {
            buff.append(": ").append(getSource().getName());
        }
        buff.append(": ").append(getPreviousRevision()).append(" -> ").append(getNewRevision());
        return buff.toString();
    }    
}