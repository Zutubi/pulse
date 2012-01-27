package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.master.model.Comment;
import com.zutubi.pulse.master.xwork.actions.project.DateModel;

/**
 * UI model for comments (on builds and agents).
 */
public class CommentModel
{
    private long id;
    private String message;
    private String author;
    private DateModel date;
    private boolean canDelete;

    public CommentModel(Comment comment, boolean canDelete)
    {
        id = comment.getId();
        message = comment.getMessage();
        author = comment.getAuthor();
        date = new DateModel(comment.getTime());
        this.canDelete = canDelete;
    }

    public long getId()
    {
        return id;
    }

    public String getMessage()
    {
        return message;
    }

    public String getAuthor()
    {
        return author;
    }

    public DateModel getDate()
    {
        return date;
    }

    public boolean isCanDelete()
    {
        return canDelete;
    }
}
