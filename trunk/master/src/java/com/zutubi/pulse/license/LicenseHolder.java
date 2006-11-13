package com.zutubi.pulse.license;

import com.zutubi.pulse.license.authorisation.AddAgentAuthorisation;

import java.util.List;
import java.util.LinkedList;

/**
 *
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
        LicenseHolder.license = l;
    }

    public static synchronized boolean hasAuthorization(String auth)
    {
        return authorisations.contains(auth);
    }

    public static synchronized void ensureAuthorization(String... auths) throws LicenseException
    {
        for (String auth : auths)
        {
            if(!hasAuthorization(auth))
            {
                throw new LicenseException();
            }
        }
    }
    
    public static synchronized void setAuthorizations(List<String> authorisations)
    {
        LicenseHolder.authorisations = authorisations;
    }
}
