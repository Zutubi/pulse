package com.zutubi.pulse.license;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public abstract class AbstractLicenseKeyStore implements LicenseKeyStore
{
    protected final List<LicenseKeyStoreListener> listeners = new LinkedList<LicenseKeyStoreListener>();

    public synchronized void register(LicenseKeyStoreListener listener)
    {
        listeners.add(listener);
    }

    public synchronized void unregister(LicenseKeyStoreListener listener)
    {
        listeners.remove(listener);
    }

    protected void notifyListeners()
    {
        List<LicenseKeyStoreListener> copy;
        synchronized(listeners)
        {
            copy = new LinkedList<LicenseKeyStoreListener>(listeners);
        }

        for (LicenseKeyStoreListener lksl : copy)
        {
            lksl.keyChanged();
        }
    }
}
