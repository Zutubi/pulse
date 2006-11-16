package com.zutubi.pulse.license;

/**
 * The LicenseKeyStore provides access to loading and storing the license key
 *
 * 
 */
public interface LicenseKeyStore
{
    /**
     * Retrieve the stored license key.
     *
     * @return license key or null if no key is configured.
     */
    String getKey();

    /**
     * Set the license key
     *
     * @param licenseKey string representing a valid license key
     */
    void setKey(String licenseKey) throws LicenseException;
}
