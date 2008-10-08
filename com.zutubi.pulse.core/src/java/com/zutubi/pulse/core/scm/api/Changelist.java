package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.StringUtils;

import java.util.*;


/**
 * Represents an atomic change committed to an SCM server.  Note that
 * changelists have been designed to be immutable.
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
        if (revision == null)
        {
            throw new NullPointerException("Revision may not be null");
        }

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

        if (time != that.time)
        {
            return false;
        }
        if (author != null ? !author.equals(that.author) : that.author != null)
        {
            return false;
        }
        if (comment != null ? !comment.equals(that.comment) : that.comment != null)
        {
            return false;
        }

        return revision.equals(that.revision);
    }

    public int hashCode()
    {
        int result;
        result = revision.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "{ rev: " + revision.toString() + ", changes: " + changes.toString() + " }";
    }
}
