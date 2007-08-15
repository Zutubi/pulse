package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.model.Revision;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The cvs revision is a composite of information used to identify a
 * particular checkin (new revision). Because cvs does not support atomic
 * commits, these revisions are a best guess.
 */
public class CvsRevision //extends Revision
{
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    private static final int MAX_COMMENT_LENGTH = 4095;
    private static final String COMMENT_TRIM_MESSAGE = "... [trimmed]";

    private String author;
    private String comment;
    private String branch;
    private long time;
    private String revisionString;

    public static final CvsRevision HEAD = null; 

    protected CvsRevision()
    {

    }

    public CvsRevision(String author, String tag, String comment, Date date)
    {
        this(author, comment, date);
        setBranch(tag);
    }

    public CvsRevision(String author, String comment, Date date)
    {
        this.author = author;
        this.comment = trimComment(comment);
        setDate(date);
        setRevisionString(generateRevisionString());
    }

    private String trimComment(String comment)
    {
        if(comment != null && comment.length() > MAX_COMMENT_LENGTH)
        {
            comment = comment.substring(0, MAX_COMMENT_LENGTH - COMMENT_TRIM_MESSAGE.length()) + COMMENT_TRIM_MESSAGE;
        }

        return comment;
    }


    public CvsRevision(String revisionString) throws ScmException
    {
        String[] parts = revisionString.split(":");
        if (parts.length == 1)
        {
            // Try a date without a time: yyyymmdd
            setDate(revisionString, revisionString, new SimpleDateFormat("yyyyMMdd"));
        }
        else if (parts.length == 3)
        {
            // Should just be a date, in DATE_FORMAT format
            setDate(revisionString, revisionString, DATE_FORMAT);
        }
        else if (parts.length == 5)
        {
            // Should be the output of generateRevisionString
            parts = revisionString.split(":", 3);

            if (parts[0].length() > 0)
            {
                setAuthor(parts[0]);
            }

            if (parts[1].length() > 0)
            {
                setBranch(parts[1]);
            }

            if (parts[2].length() > 0)
            {
                setDate(parts[2], revisionString, DATE_FORMAT);
            }
        }
        else
        {
            throw new ScmException("Invalid CVS revision '" + revisionString + "' (must be a date, or <author>:<branch>:<date>)");
        }

        setRevisionString(generateRevisionString());
    }

    private void setDate(String s, String revisionString, DateFormat dateFormat) throws ScmException
    {
        try
        {
            setDate(dateFormat.parse(s));
        }
        catch (ParseException e)
        {
            throw new ScmException("Invalid CVS revision '" + revisionString + "': date is invalid: " + e.getMessage());
        }
    }

    private String generateRevisionString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getAuthor() != null ? getAuthor() : "");
        buffer.append(":");
        buffer.append(getBranch() != null ? getBranch() : "");
        buffer.append(":");
        buffer.append((getDate() != null) ? DATE_FORMAT.format(getDate()) : "");
        return buffer.toString();
    }

    public boolean isHead()
    {
        return getAuthor() == null && getBranch() == null && getComment() == null && getDate() == null;
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
        if (getDate() == null && r.getDate() == null)
        {
            return 0;
        }
        if (getDate() == null)
        {
            return 1;
        }
        if (r.getDate() == null)
        {
            return -1;
        }
        return 0;
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

    public String getBranch()
    {
        return branch;
    }

    public void setBranch(String branch)
    {
        this.branch = branch;
    }

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
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


    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CvsRevision that = (CvsRevision) o;

        if (time != that.time) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (branch != null ? !branch.equals(that.branch) : that.branch != null) return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (revisionString != null ? !revisionString.equals(that.revisionString) : that.revisionString != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (branch != null ? branch.hashCode() : 0);
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (revisionString != null ? revisionString.hashCode() : 0);
        return result;
    }
}
