package com.zutubi.pulse.license;

import com.zutubi.pulse.bootstrap.DataResolver;
import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.license.authorisation.Authorisation;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 *
 *
 */
public class LicenseManager
{
    private DataResolver resolver;

    private List<Authorisation> authorisations = new LinkedList<Authorisation>();

    public void updateLicenseKey(String newKey) throws LicenseException
    {
        try
        {
            Data data = resolver.getData();
            data.updateLicenseKey(newKey);

            // refresh the authorisations, now that we have a new license.
            LicenseHolder.setLicense(data.getLicense());
            refreshAuthorisations();
        }
        catch (IOException e)
        {
            throw new LicenseException("Failed to update license key. Cause: " + e.getClass().getName() +
                    "; " + e.getMessage(), e);
        }
    }

    public void init()
    {
        // load the license from disk.
        Data data = resolver.getData();
        if (data != null)
        {
            LicenseHolder.setLicense(data.getLicense());
        }

        // refresh the supported authorisations.
        refreshAuthorisations();
    }

    public void refreshAuthorisations()
    {
        License license = LicenseHolder.getLicense();
        List<String> newAuths = new LinkedList<String>();
        for (Authorisation auth : authorisations)
        {
            newAuths.addAll(Arrays.asList(auth.getAuthorisation(license)));
        }
        LicenseHolder.setAuthorizations(newAuths);
    }

    /**
     * Required resource.
     *
     * @param resolver
     */
    public void setResolver(DataResolver resolver)
    {
        this.resolver = resolver;
    }

    public void setAuthorisations(List<Authorisation> a)
    {
        this.authorisations = a;
    }

    public void addAuthorisation(Authorisation auth)
    {
        this.authorisations.add(auth);
        refreshAuthorisations();
    }
}
