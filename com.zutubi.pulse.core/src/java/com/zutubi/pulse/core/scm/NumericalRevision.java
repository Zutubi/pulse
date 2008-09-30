package com.zutubi.pulse.core.scm;

import java.util.Date;

/**
 * A subversion revision, which is just a revision number.
 *
 * @author jsankey
 */
public class NumericalRevision
{
    private String author;
    private String comment;
    private String revisionString;
    private String branch;
    private Date date;

    protected NumericalRevision()
    {
    }

    public NumericalRevision(long revisionNumber)
    {
        setRevisionString(Long.toString(revisionNumber));
    }

    public NumericalRevision(String author, String comment, Date date, String revisionString)
    {
        this.author = author;
        this.comment = comment;
        this.date = date;
        this.revisionString = revisionString;
    }

    public NumericalRevision(String author, String comment, Date date, long revisionNumber)
    {
        this(author, comment, date, Long.toString(revisionNumber));
    }

    public long getRevisionNumber()
    {
        try
        {
            return Long.parseLong(getRevisionString());
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }

    public boolean isHead()
    {
        return false;
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

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public String getBranch()
    {
        return branch;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
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

        NumericalRevision that = (NumericalRevision) o;
        return !(revisionString != null ? !revisionString.equals(that.revisionString) : that.revisionString != null);
    }

    public int hashCode()
    {
        return (revisionString != null ? revisionString.hashCode() : 0);
    }
}
