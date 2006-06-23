package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.license.LicenseAuthorisation;
import com.zutubi.pulse.license.LicenseProvider;

/**
 * <class-comment/>
 */
public abstract class AbstractLicenseAuthorisation implements LicenseAuthorisation
{
    protected LicenseProvider provider;

    public void setProvider(LicenseProvider provider)
    {
        this.provider = provider;
    }
}
