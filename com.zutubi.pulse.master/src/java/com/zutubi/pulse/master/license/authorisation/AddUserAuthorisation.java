package com.zutubi.pulse.master.license.authorisation;

import com.zutubi.pulse.master.license.License;
import com.zutubi.pulse.master.license.LicenseHolder;
import com.zutubi.pulse.master.model.UserManager;

/**
 * <class-comment/>
 */
public class AddUserAuthorisation implements Authorisation
{
    private UserManager userManager;

    public static final String[] AUTH = {LicenseHolder.AUTH_ADD_USER};

    public String[] getAuthorisation(License license)
    {
        if (license == null)
        {
            return NO_AUTH;
        }

        if (license.getSupportedUsers() == License.UNRESTRICTED)
        {
            return AUTH;
        }

        if(userManager.getUserCount() < license.getSupportedUsers())
        {
            return AUTH;
        }
        return NO_AUTH;
    }

    /**
     * Required resource.
     *
     * @param userManager instance
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

}
