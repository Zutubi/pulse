package com.zutubi.pulse.master.license.authorisation;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.license.License;
import com.zutubi.pulse.master.license.LicenseHolder;

/**
 * <class-comment/>
 */
public class CanRunPulseAuthorisation implements Authorisation
{
    private static final String[] AUTH = {LicenseHolder.AUTH_RUN_PULSE};

    /**
     * Retrieve the authorisations granted by the specified license to the
     * current version of pulse.
     *
     * @param license to check
     *
     * @return an array of authorisation strings.
     */
    public String[] getAuthorisation(License license)
    {
        return getAuthorisation(license, Version.getVersion());
    }

    /**
     * Retrieve the authorisations granted by the specified license when running
     * the specified version of pulse.
     *
     * @param license to check
     * @param version being authorised
     *
     * @return an array of authorisation strings.
     */
    public String[] getAuthorisation(License license, Version version)
    {
        if (license == null)
        {
            return NO_AUTH;
        }

        if (license.canRunVersion(version))
        {
            return AUTH;
        }

        return NO_AUTH;
    }
}
