/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.User;

/**
 * <class-comment/>
 */
public class EditUserAction extends UserActionSupport
{
    private User user = new User();

    public User getUser()
    {
        return user;
    }

    public String doInput() throws Exception
    {
        // load user details.
        user = super.getUser();

        return super.doInput();
    }

    public String execute()
    {
        // load user details.
        User persistentUser = super.getUser();
        persistentUser.setName(user.getName());

        getUserManager().save(persistentUser);
        return SUCCESS;
    }
}
