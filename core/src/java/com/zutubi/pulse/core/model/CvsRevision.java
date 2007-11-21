package com.zutubi.pulse.core.model;

import com.zutubi.pulse.scm.SCMException;

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

    public CvsRevision(String revStr) throws SCMException
    {
        // special case formats:
        // a) date and time.
        try
        {
            setDate(DATE_FORMAT.parse(revStr));
            setRevisionString(generateRevisionString());
            return;
        }
        catch (ParseException e)
        {
            // noop.
        }

        // b) just a date, no time.
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        try
        {
            setDate(format.parse(revStr));
            setRevisionString(generateRevisionString());
            return;
        }
        catch (ParseException e)
        {
            // noop.
        }

        // <author>:<branch/tag>:<date>
        if (revStr == null || revStr.indexOf(":") == -1 || revStr.substring(revStr.indexOf(":")).indexOf(":") == -1)
        {
            throw new SCMException("Invalid CVS revision '" + revStr + "' (must be a date, or <author>:<branch>:<date>)");
        }
        
        String author = revStr.substring(0, revStr.indexOf(":"));
        String remainder = revStr.substring(revStr.indexOf(":") + 1);
        String branch = remainder.substring(0, remainder.indexOf(":"));
        String date = remainder.substring(remainder.indexOf(":") + 1);

        if (author != null && !author.equals(""))
        {
            setAuthor(author);
        }
        if (branch != null && !branch.equals(""))
        {
            setBranch(branch);
        }

        if (date != null && date.equals(""))
        {
            setRevisionString(generateRevisionString());
            return;
        }

        // accept two types of date format.
        try
        {
            setDate(DATE_FORMAT.parse(date));
            setRevisionString(generateRevisionString());
            return;
        }
        catch (ParseException e)
        {
            // noop.
        }

        try
        {
            setDate(format.parse(date));
            setRevisionString(generateRevisionString());
            return;
        }
        catch (ParseException e)
        {
            // noop.
        }

        if (!revStr.equals(""))
        {
            throw new SCMException("Invalid CVS revision '" + revStr + "' (must be a date, or <author>:<branch>:<date>)");
        }
        
        setRevisionString(generateRevisionString());
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
