package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseType;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.Version;

import java.util.Date;

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

        if (!license.isExpired())
        {
            return AUTH;
        }

        if (LicenseType.EVALUATION == license.getType())
        {
            // eval license expiry is a hard expiry.
            return NO_AUTH;
        }

        // can continue to run pulse on patch releases.
        Date vrd = version.getReleaseDateAsDate();
        Date expiry = license.getExpiryDate();

        if (vrd.getTime() < expiry.getTime())
        {
            return AUTH;
        }

        return NO_AUTH;
    }
}
