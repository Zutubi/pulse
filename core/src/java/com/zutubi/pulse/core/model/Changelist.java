package com.zutubi.pulse.core.model;

import com.zutubi.pulse.util.TimeStamps;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * A trivial implementation of the Changelist interface.
 *
 * @author jsankey
 */
public class Changelist extends Entity
{
    /* Unique ID for the server this revision is stored on. */
    private String serverUid;
    private Revision revision;
    private List<Change> changes;

    private long projectId;
    private long resultId;

    protected Changelist()
    {

    }

    public Changelist(String serverUid, Revision revision)
    {
        this.serverUid = serverUid;
        this.revision = revision;
        this.changes = new LinkedList<Change>();
    }

    public void addChange(Change change)
    {
        changes.add(change);
    }

    public String getServerUid()
    {
        return serverUid;
    }

    private void setServerUid(String serverUid)
    {
        this.serverUid = serverUid;
    }

    public Revision getRevision()
    {
        return revision;
    }

    public Date getDate()
    {
        return getRevision().getDate();
    }

    public String getPrettyTime()
    {
        return TimeStamps.getPrettyTime(getRevision().getDate().getTime());
    }

    public String getPrettyDate(Locale locale)
    {
        return TimeStamps.getPrettyDate(getRevision().getDate().getTime(), locale);
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

    public String toString()
    {
        return "{ uid: " + serverUid + ", rev: " + revision.toString() + ", changes: " + changes.toString() + " }";
    }
}
