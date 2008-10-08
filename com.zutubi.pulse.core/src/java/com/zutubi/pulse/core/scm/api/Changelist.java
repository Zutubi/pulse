package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.StringUtils;

import java.util.*;


/**
 * Represents an atomic change committed to an SCM server.
 */
public class Changelist implements Comparable<Changelist>
{
    private static final int MAX_COMMENT_LENGTH = 4095;
    private static final String COMMENT_TRIM_MESSAGE = "... [trimmed]";

    private Revision revision;
    private long time;
    private String author;
    private String comment;

    private List<FileChange> changes;

    public Changelist(Revision revision, long time, String author, String comment, Collection<FileChange> changes)
    {
        this.revision = revision;
        this.time = time;
        this.author = author;

        if (comment != null)
        {
            this.comment = StringUtils.trimmedString(comment, MAX_COMMENT_LENGTH, COMMENT_TRIM_MESSAGE);
        }

        this.changes = new LinkedList<FileChange>(changes);
    }

    public Revision getRevision()
    {
        return revision;
    }

    public long getTime()
    {
        return time;
    }

    public Date getDate()
    {
        return new Date(time);
    }

    public String getAuthor()
    {
        return author;
    }

    public String getComment()
    {
        return comment;
    }

    public List<FileChange> getChanges()
    {
        return Collections.unmodifiableList(changes);
    }

    public int compareTo(Changelist o)
    {
        if (time > o.time)
        {
            return 1;
        }
        else if (time < o.time)
        {
            return -1;
        }

        return 0;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Changelist that = (Changelist) o;
        return revision.equals(that.revision);

    }

    public int hashCode()
    {
        return revision.hashCode();
    }

    public String toString()
    {
        return "{ rev: " + revision.toString() + ", changes: " + changes.toString() + " }";
    }
}
