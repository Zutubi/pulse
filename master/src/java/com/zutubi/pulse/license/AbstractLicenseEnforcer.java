package com.zutubi.pulse.license;

/**
 * <class-comment/>
 */
public abstract class AbstractLicenseEnforcer implements LicenseEnforcer
{
    protected LicenseProvider provider;

    public void setProvider(LicenseProvider provider)
    {
        this.provider = provider;
    }
}
