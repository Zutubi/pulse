/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.web.user.UserActionSupport;

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
            addActionError("Unknown user [" + getUserId() + "]");
        }
    }
}
