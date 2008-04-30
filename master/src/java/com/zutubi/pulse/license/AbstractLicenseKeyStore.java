package com.zutubi.pulse.license;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public abstract class AbstractLicenseKeyStore implements LicenseKeyStore
{
    protected final List<LicenseKeyStoreListener> listeners = new LinkedList<LicenseKeyStoreListener>();

    public void register(LicenseKeyStoreListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(listener);
        }
    }

    public void unregister(LicenseKeyStoreListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(listener);
        }
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
