/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.zutubi.i18n.Messages;

/**
 * Holds information about the user that is responsible for a project.  This
 * mechanism is typically used by developers to notify their colleagues that
 * they are working on a fixing a build issue.
 */
public class ProjectResponsibility
{
    private static final Messages I18N = Messages.getInstance(ProjectResponsibility.class);

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
            return I18N.format("responsible.self");
        }
        else
        {
            return I18N.format("responsible.other", user.getLogin());
        }
    }
}
