package com.zutubi.pulse.web.admin.user;

import static com.zutubi.pulse.model.GrantedAuthority.ADMINISTRATOR;
import com.zutubi.pulse.web.user.UserActionSupport;

/**
 * 
 *
 */
public class ViewUserAction extends UserActionSupport
{
    public boolean admin;

    public boolean isAdmin()
    {
        return admin;
    }

    public void validate()
    {
        if (getUser() == null)
        {
            addActionError("Unknown user [" + getUserId() + "]");
        }
    }

    public String execute() throws Exception
    {
        admin = getUser().hasAuthority(ADMINISTRATOR);
        return SUCCESS;
    }
}
