package com.zutubi.pulse.license;

/**
 * <class-comment/>
 */
public class EvaluationLicenseManager implements LicenseManager
{
    LicenseProvider provider;

    public void setProvider(LicenseProvider provider)
    {
        this.provider = provider;
    }

    public boolean canBuild()
    {
        return !provider.getLicense().isExpired();
    }

    public boolean canAddProject()
    {
        return !provider.getLicense().isExpired();
    }

    public boolean canAddUser()
    {
        return !provider.getLicense().isExpired();
    }

    public boolean canAddAgent()
    {
        return !provider.getLicense().isExpired();
    }
}
