package com.zutubi.pulse.core.model;

import java.util.Date;

/**
 *
 *
 */
public class Revision extends Entity implements Comparable<Revision>
{
    private static final int MAX_COMMENT_LENGTH = 4095;
    private static final String COMMENT_TRIM_MESSAGE = "... [trimmed]";

    private String author;
    private String comment;
    private String branch;
    private long time;
    private String revisionString;

    protected Revision()
    {
    }

    public Revision(String author, String comment, Date date)
    {
        this.author = author;
        this.comment = trimComment(comment);
        setDate(date);
    }

    private String trimComment(String comment)
    {
        if(comment != null && comment.length() > MAX_COMMENT_LENGTH)
        {
            comment = comment.substring(0, MAX_COMMENT_LENGTH - COMMENT_TRIM_MESSAGE.length()) + COMMENT_TRIM_MESSAGE;
        }

        return comment;
    }

    protected void copyCommon(Revision copy)
    {
        copy.author = author;
        copy.comment = comment;
        copy.branch = branch;
        copy.time = time;
        copy.revisionString = revisionString;
    }

    public Revision copy()
    {
        Revision copy = new Revision();
        copyCommon(copy);
        return copy;
    }

    /**
     * The author of change
     */
    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * The comment associated with this change.
     */
    public String getComment()
    {
        return comment;
    }

    private void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * The branch on which this change was made.
     */
    public String getBranch()
    {
        return branch;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    /**
     * The date of this change.
     */
    public Date getDate()
    {
        if (time > 0)
        {
            return new Date(time);
        }
        return null;
    }

    public void setDate(Date date)
    {
        if (date != null)
        {
            this.time = date.getTime();
        }
        else
        {
            this.time = -1;
        }
    }

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public String toString()
    {
        return getRevisionString();
    }

    /**
     * Used by hibernate to persist the time.
     */
    private long getTime()
    {
        return time;
    }

    /**
     * Used by hibernate to persist the time.
     */
    private void setTime(long time)
    {
        this.time = time;
    }

    public int compareTo(Revision r)
    {
        // First try basing on time
        if (getDate() != null && r.getDate() != null)
        {
            int result = getDate().compareTo(getDate());
            if (result != 0)
            {
                return result;
            }
        }

        // OK, is the revision string a number?  If so, use it
        try
        {
            long n1 = Long.parseLong(revisionString);
            long n2 = Long.parseLong(r.revisionString);

            return (int) (n1 - n2);
        }
        catch (NumberFormatException e)
        {
            // Bummer, not numerical revisions
        }

        // Oh well, go for lexical ordering then
        return revisionString.compareTo(r.revisionString);
    }

    public boolean equals(Object other)
    {
        if(other == null || !(other instanceof Revision))
        {
            return false;
        }

        return revisionString.equals(((Revision)other).revisionString);
    }

    public int hashCode()
    {
        return revisionString.hashCode();
    }
}
