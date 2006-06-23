package com.zutubi.pulse.license.authorisation;

/**
 * <class-comment/>
 */
public class EvaluationLicenseAuthorisation extends AbstractLicenseAuthorisation
{
    public boolean canRunPulse()
    {
        return !provider.getLicense().isExpired();
    }

    public boolean canAddProject()
    {
        return canRunPulse();
    }

    public boolean canAddUser()
    {
        return canRunPulse();
    }

    public boolean canAddAgent()
    {
        return canRunPulse();
    }
}
