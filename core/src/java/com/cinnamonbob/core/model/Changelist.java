package com.cinnamonbob.core.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A trivial implementation of the Changelist interface.
 *
 * @author jsankey
 */
public class Changelist extends Entity
{
    private Revision revision;
    private List<Change> changes;

    protected Changelist()
    {

    }

    public Changelist(Revision revision)
    {
        this.revision = revision;
        this.changes = new LinkedList<Change>();
    }

    public void addChange(Change change)
    {
        changes.add(change);
    }

    public Revision getRevision()
    {
        return revision;
    }

    public Date getDate()
    {
        return getRevision().getDate();
    }

    public String getUser()
    {
        return getRevision().getAuthor();
    }

    public String getComment()
    {
        return getRevision().getComment();
    }

    public List<Change> getChanges()
    {
        return changes;
    }

    private void setChanges(List<Change> changes)
    {
        this.changes = changes;
    }

    private void setRevision(Revision revision)
    {
        this.revision = revision;
    }
}
