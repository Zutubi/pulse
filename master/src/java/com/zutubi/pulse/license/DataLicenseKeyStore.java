package com.zutubi.pulse.license;

import com.zutubi.pulse.bootstrap.DataResolver;
import com.zutubi.pulse.bootstrap.Data;

/**
 * An implementation of the key store that uses the data directory to store the key.
 *
 */
public class DataLicenseKeyStore implements LicenseKeyStore
{
    private DataResolver dataResolver;

    public String getKey()
    {
        Data data = dataResolver.getData();
        if (data == null)
        {
            // data is not ready.
            return null;
        }

        return data.getLicenseKey();
    }

    /**
     * Store the specified license key
     *
     * @param licenseKey new license key string
     *
     * @throws LicenseException is the license store is not yet available, the case when the data directory
     * has not been specified.
     */
    public void setKey(String licenseKey) throws LicenseException
    {
        Data data = dataResolver.getData();
        if (data == null)
        {
            throw new LicenseException("Key store is not ready.");
        }
        data.setLicenseKey(licenseKey);
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
