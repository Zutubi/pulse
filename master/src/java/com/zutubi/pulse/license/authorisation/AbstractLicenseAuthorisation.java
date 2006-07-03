package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.license.LicenseAuthorisation;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.LicenseProvider;

/**
 * <class-comment/>
 */
public abstract class AbstractLicenseAuthorisation implements LicenseAuthorisation
{
    private LicenseManager licenseManager;

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    protected LicenseProvider getProvider()
    {
        return licenseManager;
    }
}
