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

    public String[] getAuthorisation(License license)
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
        Version currentVersion = Version.getVersion();
        Date vrd = currentVersion.getReleaseDateAsDate();
        Date expiry = license.getExpiryDate();

        if (vrd.getTime() < expiry.getTime())
        {
            return AUTH;
        }

        return NO_AUTH;
    }
}
