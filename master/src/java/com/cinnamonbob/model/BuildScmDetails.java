package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds build model information for single SCM, e.g. changes committed to
 * that SCM since the last build.
 */
public class BuildScmDetails extends Entity
{
    private String scmName;
    private Revision revision;
    private List<Changelist> changelists;

    public BuildScmDetails()
    {

    }

    public BuildScmDetails(String scmName, Revision revision, List<Changelist> changelists)
    {
        this.scmName = scmName;
        this.revision = revision;
        this.changelists = changelists;
    }

    public String getScmName()
    {
        return scmName;
    }

    private void setScmName(String scmName)
    {
        this.scmName = scmName;
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
