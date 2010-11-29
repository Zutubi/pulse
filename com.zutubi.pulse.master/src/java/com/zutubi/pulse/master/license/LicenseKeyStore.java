package com.zutubi.pulse.master.license;

/**
 * The LicenseKeyStore provides access to loading and storing the license key
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
     *
     * @throws LicenseException if there is a problem storing the license key.
     */
    void setKey(String licenseKey) throws LicenseException;

    /**
     * Register a listener to be notified when the license key is changed.
     *
     * @param listener that will receive the notifications
     */
    void register(LicenseKeyStoreListener listener);

    /**
     * Unregister a license so that it is no longer notified on license key changes.
     *
     * @param listener that was previously registered.
     */
    void unregister(LicenseKeyStoreListener listener);
}
