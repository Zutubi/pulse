package com.zutubi.pulse.master.license;

import java.util.LinkedList;
import java.util.List;

/**
 * Singleton to hold the server license.
 */
public class LicenseHolder
{
    public static final String AUTH_ADD_AGENT = "canAddAgent";
    public static final String AUTH_ADD_PROJECT = "canAddProject";
    public static final String AUTH_ADD_USER = "canAddUser";
    public static final String AUTH_RUN_PULSE = "canRunPulse";

    private static License license;

    private static List<String> authorisations = new LinkedList<String>();

    public static synchronized License getLicense()
    {
        return license;
    }

    public static synchronized void setLicense(License l)
    {
        license = l;
    }

    public static synchronized boolean hasAuthorization(String... auths)
    {
        for (String auth : auths)
        {
            if (!authorisations.contains(auth))
            {
                return false;
            }
        }
        return true;
    }

    public static synchronized void ensureAuthorization(String... auths) throws LicenseException
    {
        for (String auth : auths)
        {
            if(!hasAuthorization(auth))
            {
                throw new LicenseException("Missing authorisation: " + auth);
            }
        }
    }

    public static synchronized void setAuthorizations(List<String> authorisations)
    {
        LicenseHolder.authorisations = authorisations;
    }
}
