package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A trivial implementation of the Changelist interface.
 *
 * @author jsankey
 */
public class PersistentChangelist extends Entity
{
    private Revision revision;
    private long time;
    private String author;
    private String comment;
    private List<PersistentFileChange> changes;

    private long projectId;
    private long resultId;

    protected PersistentChangelist()
    {

    }

    public PersistentChangelist(Revision revision, long time, String author, String comment, Collection<PersistentFileChange> changes)
    {
        this.revision = revision;
        this.time = time;
        this.author = author;
        this.comment = comment;
        this.changes = new LinkedList<PersistentFileChange>(changes);
    }

    public PersistentChangelist(Changelist data)
    {
        revision = data.getRevision();
        time = data.getTime();
        author = data.getAuthor();
        comment = data.getComment();
        changes = CollectionUtils.map(data.getChanges(), new Mapping<FileChange, PersistentFileChange>()
        {
            public PersistentFileChange map(FileChange change)
            {
                return new PersistentFileChange(change);
            }
        });
    }

    public Changelist asChangelist()
    {
        return new Changelist(revision, time, author, comment, CollectionUtils.map(changes, new Mapping<PersistentFileChange, FileChange>()
        {
            public FileChange map(PersistentFileChange persistentFileChange)
            {
                return persistentFileChange.asChange();
            }
        }));
    }
    
    public Date getDate()
    {
        return new Date(time);
    }

    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public List<PersistentFileChange> getChanges()
    {
        return changes;
    }

    public void setChanges(List<PersistentFileChange> changes)
    {
        this.changes = changes;
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
        return "{ rev: " + ((revision != null) ? revision.toString() : "null") + ", changes: " + changes.toString() + " }";
    }
}
