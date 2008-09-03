package com.zutubi.pulse.core.scm.git;

/**
 *
 *
 */
public class GitLogEntry
{
    private String commit = "";
    private String author = "";
    private String date = "";
    private String comment = "";

    public String getCommit()
    {
        return commit;
    }

    public void setCommit(String commit)
    {
        this.commit = commit;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
