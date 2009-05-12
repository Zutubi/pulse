package com.zutubi.pulse.master.model;

import com.zutubi.i18n.Messages;

/**
 * Holds information about the user that is responsible for a project.  This
 * mechanism is typically used by developers to notify their colleagues that
 * they are working on a fixing a build issue.
 */
public class ProjectResponsibility
{
    private User user;
    private String comment;

    public ProjectResponsibility()
    {
    }

    public ProjectResponsibility(User user, String comment)
    {
        this.user = user;
        this.comment = comment;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * Gets a message describing this responsibility from the perspective of
     * the given user.  The message changes depending on whether it is the
     * user themself or another user who is responsible.
     *
     * @param loggedInUser the user to tailor the message for
     * @return a description of this from the perspective of the given user
     */
    public String getMessage(User loggedInUser)
    {
        if (user.equals(loggedInUser))
        {
            return Messages.format(ProjectResponsibility.class, "responsible.self");
        }
        else
        {
            return Messages.format(ProjectResponsibility.class, "responsible.other", user.getLogin());
        }
    }
}
