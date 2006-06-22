package com.zutubi.pulse.license;

/**
 * <class-comment/>
 */
public abstract class AbstractLicenseManager implements LicenseManager
{
    protected LicenseProvider provider;

    public void setProvider(LicenseProvider provider)
    {
        this.provider = provider;
    }
}
