package com.zutubi.pulse.core.model;

import java.text.DateFormat;
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
        setAuthor(author);
        setBranch(tag);
        setComment(comment);
        setDate(date);

        // construct the revision string.
        StringBuffer buffer = new StringBuffer();
        buffer.append(getAuthor() != null ? getAuthor() : "");
        buffer.append(":");
        buffer.append(getBranch() != null ? getBranch() : "");
        buffer.append(":");
        buffer.append((date != null) ? DATE_FORMAT.format(date) : "");
        setRevisionString(buffer.toString());
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
