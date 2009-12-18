package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Comment;
import com.zutubi.pulse.master.model.User;

/**
 * Action allowing a user to add a message to a build result.
 */
public class AddCommentAction extends CommentActionBase
{
    private String message;

    public void setMessage(String message)
    {
        this.message = message;
    }

    @Override
    protected void updateBuild(BuildResult build, User user)
    {
        Comment comment = new Comment(user.getLogin(), System.currentTimeMillis(), message);
        build.addComment(comment);
    }

}
