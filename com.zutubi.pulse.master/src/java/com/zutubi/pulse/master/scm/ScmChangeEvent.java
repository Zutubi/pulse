package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.project.events.ProjectEvent;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * This event indicates that a change has been detected in an SCM.
 */
public class ScmChangeEvent extends ProjectEvent
{
    private Revision newRevision;
    private Revision previousRevision;

    public ScmChangeEvent(ProjectConfiguration source, Revision newRevision, Revision previousRevision)
    {
        super(source, source);
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
            buff.append(": ").append((getProjectConfiguration()).getName());
        }
        buff.append(": ").append(getPreviousRevision()).append(" -> ").append(getNewRevision());
        return buff.toString();
    }
}