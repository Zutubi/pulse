package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;

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
        user = getUser();
        return super.doInput();
    }

    public String execute()
    {
        User persistentUser = getUser();
        persistentUser.setName(user.getName());
        getUserManager().save(persistentUser);
        return SUCCESS;
    }
}
