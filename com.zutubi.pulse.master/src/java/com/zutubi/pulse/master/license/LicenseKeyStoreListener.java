package com.zutubi.pulse.master.license;

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
