package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.license.LicenseAuthorisation;

/**
 * <class-comment/>
 */
public class NoAuthorisation implements LicenseAuthorisation
{
    public boolean canRunPulse()
    {
        return false;
    }

    public boolean canAddProject()
    {
        return false;
    }

    public boolean canAddUser()
    {
        return false;
    }

    public boolean canAddAgent()
    {
        return false;
    }
}
