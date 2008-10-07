package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TimeStamps;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


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

    private List<Change> changes;

    protected Changelist()
    {
    }

    public Changelist(Revision revision, long time, String author, String comment)
    {
        this.revision = revision;
        this.time = time;
        this.author = author;

        if (comment != null)
        {
            this.comment = StringUtils.trimmedString(comment, MAX_COMMENT_LENGTH, COMMENT_TRIM_MESSAGE);
        }

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
        return new Date(time);
    }

    public String getPrettyTime()
    {
        return TimeStamps.getPrettyTime(time);
    }

    public String getPrettyDate(Locale locale)
    {
        return TimeStamps.getPrettyDate(time, locale);
    }

    public String getAuthor()
    {
        return author;
    }

    public String getComment()
    {
        return comment;
    }

    public List<Change> getChanges()
    {
        return changes;
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
