package com.zutubi.pulse.license;

import java.util.List;
import java.util.LinkedList;

/**
 *
 */
public class LicenseHolder
{
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

    public static synchronized void setAuthorizations(List<String> authorisations)
    {
        LicenseHolder.authorisations = authorisations;
    }
}
