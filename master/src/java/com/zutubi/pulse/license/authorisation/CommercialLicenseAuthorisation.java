package com.zutubi.pulse.license.authorisation;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.license.License;

import java.util.Date;

/**
 * <class-comment/>
 */
public class CommercialLicenseAuthorisation extends CustomLicenseAuthorisation
{
    public boolean canRunPulse()
    {
        License l = getProvider().getLicense();
        if (!l.isExpired())
        {
            return true;
        }

        // can continue to run pulse on patch releases.
        Version currentVersion = Version.getVersion();
        Date vrd = currentVersion.getReleaseDateAsDate();
        Date expiry = l.getExpiryDate();

        return (vrd.getTime() < expiry.getTime());
    }
}
