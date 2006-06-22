package com.zutubi.pulse.license;

/**
 * <class-comment/>
 */
public class CommercialLicenseEnforcer extends AbstractLicenseEnforcer
{
    public boolean isLicensed()
    {
        License l = provider.getLicense();
        if (!l.isExpired())
        {
            return true;
        }
        // else is this a point release for a release from before the expiry date?

        return false;
    }

    public boolean canAddProject()
    {
        //TODO: consider the number of projects supported by the license
        return isLicensed();
    }

    public boolean canAddUser()
    {
        //TODO: consider the number of users supported by the license
        return isLicensed();
    }

    public boolean canAddAgent()
    {
        //TODO: consider the number of agents supported by the license
        return isLicensed();
    }
}
