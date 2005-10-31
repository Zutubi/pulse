package com.cinnamonbob.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds build result information for single SCM, e.g. changes committed to
 * that SCM since the last build.
 */
public class BuildScmDetails extends Entity
{
    private Revision revision;
    private List<Changelist> changelists;


    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    public List<Changelist> getChangelists()
    {
        if(changelists == null)
        {
            changelists = new LinkedList<Changelist>();
        }
        return changelists;
    }

    public void setChangelists(List<Changelist> changelists)
    {
        this.changelists = changelists;
    }

    public void add(Changelist changelist)
    {
        getChangelists().add(changelist);
    }
}
