package com.cinnamonbob.web.user;

import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.web.ActionSupport;

/**
 *
 *
 */
public class DeleteContactPointAction extends ActionSupport
{
    private long id;
    private String name;
    private UserManager userManager;

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String execute()
    {
        User user = userManager.getUser(id);
        if (user == null)
        {
            return INPUT;
        }

        ContactPoint contactPoint = user.getContactPoint(name);
        if (contactPoint != null)
        {
            user.remove(contactPoint);
        }
        return SUCCESS;
    }
}
