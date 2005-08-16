package com.cinnamonbob.model;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * The cvs revision is a composite of information used to identify a
 * particular checkin (new revision). Because cvs does not support atomic
 * commits, these revisions are a best guess.
 */
public class CvsRevision extends Revision
{
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    protected CvsRevision()
    {

    }

    public CvsRevision(String author, String branch, String comment, Date date)
    {
        setAuthor(author);
        setBranch((branch != null) ? branch : "");
        setComment(comment);
        setDate(date);

        // construct the revision string.
        StringBuffer buffer = new StringBuffer();
        buffer.append(getAuthor());
        buffer.append(":");
        buffer.append(getBranch());
        buffer.append(":");
        buffer.append(DATE_FORMAT.format(date));
        setRevisionString(buffer.toString());
    }
}
