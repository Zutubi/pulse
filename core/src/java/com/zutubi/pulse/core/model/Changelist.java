/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.util.TimeStamps;

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
    private long projectId;
    /**
     * Id of the build result this is part of: the BuildResult class is up
     * in master so we can't map it directly.  In any case, this forces lazy
     * loading of the result which is a Good Thing.
     */
    private long resultId;

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

    public String getPrettyDate()
    {
        return TimeStamps.getPrettyTime(getRevision().getDate().getTime());
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

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getResultId()
    {
        return resultId;
    }

    public void setResultId(long resultId)
    {
        this.resultId = resultId;
    }
}
