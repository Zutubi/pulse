package com.cinnamonbob.model;

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
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    public static final CvsRevision HEAD = new CvsRevision(null, null, null, null);

    protected CvsRevision()
    {

    }

    public CvsRevision(String author, String branch, String comment, Date date)
    {
        setAuthor(author);
        setBranch(branch);
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
}
