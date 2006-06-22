package com.zutubi.pulse.license;

/**
 * <class-comment/>
 */
public class EvaluationLicenseEnforcer extends AbstractLicenseEnforcer
{
    public boolean isLicensed()
    {
        return !provider.getLicense().isExpired();
    }

    public boolean canAddProject()
    {
        return isLicensed();
    }

    public boolean canAddUser()
    {
        return isLicensed();
    }

    public boolean canAddAgent()
    {
        return isLicensed();
    }
}
