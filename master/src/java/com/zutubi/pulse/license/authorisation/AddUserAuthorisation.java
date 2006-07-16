package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.license.License;

/**
 * <class-comment/>
 */
public class AddUserAuthorisation implements Authorisation
{
    private UserManager userManager;

    public static final String[] AUTH = {"canAddUser"};

    public String[] getAuthorisation(License license)
    {
        if (license == null)
        {
            return new String[0];
        }

        if (license.getSupportedUsers() == License.UNRESTRICTED)
        {
            return AUTH;
        }

        if(userManager.getUserCount() < license.getSupportedUsers())
        {
            return AUTH;
        }
        return new String[0];
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

}
