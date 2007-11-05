package com.zutubi.pulse.license.config;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.bootstrap.DataResolver;
import com.zutubi.pulse.license.AbstractLicenseKeyStore;
import com.zutubi.pulse.license.LicenseException;

/**
 * This is a 1.x backwards compatible key store that allows access to the 1.x license in 2.x
 *
 */
public class OneXDataLicenseKeyStore extends AbstractLicenseKeyStore
{
    private DataResolver dataResolver;

    private static final String LICENSE_KEY = "license.key";

    public String getKey()
    {
        Data data = dataResolver.getData();
        if (data == null)
        {
            // data is not ready.
            return null;
        }

        return data.getConfig().getProperty(LICENSE_KEY);
    }

    /**
     * Store the specified license key
     *
     * @param licenseKey new license key string
     *
     * @throws com.zutubi.pulse.license.LicenseException is the license store is not yet available, the case when the data directory
     * has not been specified.
     */
    public void setKey(String licenseKey) throws LicenseException
    {
        Data data = dataResolver.getData();
        if (data == null)
        {
            throw new LicenseException("Key store is not ready.");
        }
        data.getConfig().setProperty(LICENSE_KEY, licenseKey);
    }

    /**
     * Required resource
     *
     * @param dataResolver instance
     */
    public void setDataResolver(DataResolver dataResolver)
    {
        this.dataResolver = dataResolver;
    }
}

