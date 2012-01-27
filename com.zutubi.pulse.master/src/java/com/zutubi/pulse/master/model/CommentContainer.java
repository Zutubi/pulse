package com.zutubi.pulse.master.model;

import java.util.List;

/**
 * Abstraction over entities that have a collection of comments.
 */
public interface CommentContainer
{
    String ACTION_ADD_COMMENT = "addComment";

    /**
     * @return the comments on this container
     */
    List<Comment> getComments();

    /**
     * Adds a new comment to the end of the list.
     *
     * @param comment the comment to add
     */
    void addComment(Comment comment);

    /**
     * Deletes a comment, if it exists.
     *
     * @param comment the comment to delete
     * @return true if the comment was found and deleted, false if it was not found
     */
    boolean removeComment(Comment comment);
}
