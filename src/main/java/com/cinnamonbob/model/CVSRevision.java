package com.cinnamonbob.model;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * The cvs revision is a composite of information used to identify a
 * particular checkin (new revision). Because cvs does not support atomic
 * commits, these revisions are a best guess.
 */
public class CvsRevision extends Revision
{
    private static final String AUTHOR = "author";
    private static final String BRANCH = "branch";
    private static final String COMMENT = "comment";
    private static final String DATE = "date";

    protected CvsRevision()
    {

    }

    public CvsRevision(String author, String branch, String comment, Date date)
    {
        setAuthor(author);
        setBranch((branch != null) ? branch : "");
        setComment(comment);
        setDate(date);
    }

    /**
     * The author of change
     *
     */
    public String getAuthor()
    {
        return getProperties().getProperty(AUTHOR);
    }

    /**
     * The branch on which this change was made.
     *
     */
    public String getBranch()
    {
        return getProperties().getProperty(BRANCH);
    }

    /**
     * The comment associated with this change.
     *
     */
    public String getComment()
    {
        return getProperties().getProperty(COMMENT);
    }

    /**
     * The date of this change.
     *
     */
    public Date getDate()
    {
        String milliStr = getProperties().getProperty(DATE);
        Date date = null;

        try
        {
            date = new Date(Long.parseLong(milliStr));
        }
        catch(NumberFormatException e)
        {
            // Daniel promises this will never happen.
        }

        return date;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getAuthor());
        buffer.append(":");
        buffer.append(getBranch());
        buffer.append(":");
        buffer.append(getComment());
        return buffer.toString();
    }

    private void setAuthor(String author)
    {
        getProperties().put(AUTHOR, author);
    }

    private void setBranch(String branch)
    {
        getProperties().put(BRANCH, branch);
    }

    private void setComment(String comment)
    {
        getProperties().put(COMMENT, comment);
    }

    private void setDate(Date date)
    {
        long millis = date.getTime();
        getProperties().setProperty(DATE, Long.toString(millis));
    }
}
