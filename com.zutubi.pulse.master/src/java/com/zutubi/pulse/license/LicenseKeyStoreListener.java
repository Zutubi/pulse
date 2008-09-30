package com.zutubi.pulse.license;

/**
 *
 *
 */
public interface LicenseKeyStoreListener
{
    /**
     * This method is called when the key stored in the license key store is changed.
     */
    void keyChanged();
}
