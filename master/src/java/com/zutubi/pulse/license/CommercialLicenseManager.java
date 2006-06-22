package com.zutubi.pulse.license;

/**
 * <class-comment/>
 */
public class CommercialLicenseManager extends AbstractLicenseManager
{
    public boolean canBuild()
    {
        return isEnabled();
    }

    public boolean canAddProject()
    {
        return isEnabled();
    }

    public boolean canAddUser()
    {
        return isEnabled();
    }

    public boolean canAddAgent()
    {
        return isEnabled();
    }

    private boolean isEnabled()
    {
        License l = provider.getLicense();
        if (!l.isExpired())
        {
            return true;
        }
        // else is this a point release for a release from before the expiry date?
        

        return false;
    }
}
