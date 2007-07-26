package com.zutubi.pulse.core.model;

import com.zutubi.pulse.scm.ScmException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The cvs revision is a composite of information used to identify a
 * particular checkin (new revision). Because cvs does not support atomic
 * commits, these revisions are a best guess.
 */
public class CvsRevision extends Revision
{
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    public static final CvsRevision HEAD = new CvsRevision(null, null, null, null);

    protected CvsRevision()
    {

    }

    public CvsRevision(String author, String tag, String comment, Date date)
    {
        super(author, comment, date);
        setBranch(tag);
        // construct the revision string.
        setRevisionString(generateRevisionString());
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

    public Revision copy()
    {
        CvsRevision copy = new CvsRevision();
        copyCommon(copy);
        return copy;
    }

    public boolean isHead()
    {
        return getAuthor() == null && getBranch() == null && getComment() == null && getDate() == null;
    }
}
