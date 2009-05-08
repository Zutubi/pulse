package com.zutubi.pulse.master.model;

import com.zutubi.i18n.Messages;

/**
 * Holds information about the user that is responsible for a build.  This
 * mechanism is typically used by developers to notify their colleagues that
 * they are working on a fixing a build issue.
 */
public class BuildResponsibility
{
    private User user;
    private String comment;

    public BuildResponsibility()
    {
    }

    public BuildResponsibility(User user, String comment)
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

    public String getMessage(User loggedInUser)
    {
        if (user.equals(loggedInUser))
        {
            return Messages.format(BuildResult.class, "responsible.self");
        }
        else
        {
            return Messages.format(BuildResult.class, "responsible.other", user.getLogin());
        }
    }
}
