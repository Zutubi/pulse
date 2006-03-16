package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.web.user.UserActionSupport;

/**
 * 
 *
 */
public class ViewUserAction extends UserActionSupport
{
    public void validate()
    {
        if (getUser() == null)
        {
            addUnknownUserFieldError();
        }
    }
}
