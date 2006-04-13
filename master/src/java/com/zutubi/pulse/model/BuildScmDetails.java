/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.Revision;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds build model information for single SCM, e.g. changes committed to
 * that SCM since the last build.
 */
public class BuildScmDetails extends Entity
{
    private Revision revision;
    private List<Changelist> changelists;

    public BuildScmDetails()
    {

    }

    public BuildScmDetails(Revision revision, List<Changelist> changelists)
    {
        this.revision = revision;
        this.changelists = changelists;
    }

    public Revision getRevision()
    {
        return revision;
    }

    private void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    public List<Changelist> getChangelists()
    {
        if (changelists == null)
        {
            changelists = new LinkedList<Changelist>();
        }
        return changelists;
    }

    private void setChangelists(List<Changelist> changelists)
    {
        this.changelists = changelists;
    }

    public void add(Changelist changelist)
    {
        getChangelists().add(changelist);
    }
}
