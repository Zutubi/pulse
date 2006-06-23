package com.zutubi.pulse.license.authorisation;

/**
 * <class-comment/>
 */
public class NonProfitLicenseAuthorisation extends AbstractLicenseAuthorisation
{
    public boolean canRunPulse()
    {
        return true;
    }

    public boolean canAddProject()
    {
        return true;
    }

    public boolean canAddUser()
    {
        return true;
    }

    public boolean canAddAgent()
    {
        return true;
    }
}
