package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.scm.Revision;

import java.util.Date;

/**
 * The cvs revision is a composite of information used to identify a
 * particular checkin (new revision). Because cvs does not support atomic
 * commits, these revisions are a best guess.
 */
public class CVSRevision implements Revision
{
    private final String author;
    private final String branch;
    private final String comment;
    private final Date date;

    public CVSRevision(String author, String branch, String comment, Date date)
    {
        this.author = author;
        this.branch = (branch != null) ? branch : "";
        this.comment = comment;
        this.date = date;
    }

    /**
     * The author of change
     *
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * The branch on which this change was made.
     *
     */
    public String getBranch()
    {
        return branch;
    }

    /**
     * The comment associated with this change.
     *
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * The date of this change.
     *
     */
    public Date getDate()
    {
        return date;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(author);
        buffer.append(":");
        buffer.append(branch);
        buffer.append(":");
        buffer.append(comment);
        return buffer.toString();
    }
}
