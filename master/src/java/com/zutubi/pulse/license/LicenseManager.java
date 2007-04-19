package com.zutubi.pulse.license;

import com.zutubi.pulse.events.DataDirectoryChangedEvent;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.license.authorisation.Authorisation;
import com.zutubi.pulse.license.authorisation.CanRunPulseAuthorisation;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.Version;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The License manager handles all things license related.
 *
 */
public class LicenseManager
{
    private static final Logger LOG = Logger.getLogger(LicenseManager.class);

    /**
     * Access to the event system
     */
    private EventManager eventManager;

    private LicenseKeyStore keyStore;

    private List<Authorisation> authorisations = new LinkedList<Authorisation>();

    /**
     * Update the installed license key
     *
     * @param newKey the new license key string
     *
     * @throws LicenseException if the specified license can not be installed.
     */
    public void installLicense(String newKey) throws LicenseException
    {
        // check that the license key is valid.
        LicenseDecoder decoder = new LicenseDecoder();
        License license = decoder.decode(newKey.getBytes());

        // persist the license key
        keyStore.setKey(newKey);

        // refresh the authorisations, now that we have a new license.
        LicenseHolder.setLicense(license);
        refreshAuthorisations();

        eventManager.publish(new LicenseUpdateEvent(license));
    }

    /**
     * Initialise the license manager.
     */
    public void init()
    {
        // the license manager monitors for changes in the system data directory. We 'know' this is
        // where the license is stored, so if there is a change, we need to refresh.  This SHOULD be reflected
        // in the licenseKeyStore / licenseManager interaction somehow since it is the license key store that
        // is using the data directory for storage purposes.
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                refresh();
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{DataDirectoryChangedEvent.class};
            }
        });
        
        refresh();
    }

    private void refresh()
    {
        String key = keyStore.getKey();
        if (key != null)
        {
            try
            {
                LicenseDecoder decoder = new LicenseDecoder();
                License license = decoder.decode(key.getBytes());
                LicenseHolder.setLicense(license);
            }
            catch (LicenseException e)
            {
                // license key is invalid.
                LOG.warning("Failed to decode the configured license key.", e);
                LicenseHolder.setLicense(null);
            }
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
     * Check whether or not the currently installed license is able to run the specified version of pulse.
     *
     * @param version to check
     * 
     * @return true if the installed license can run the specified version, false otherwise.
     */
    public boolean canRun(Version version)
    {
        CanRunPulseAuthorisation canRunPulse = new CanRunPulseAuthorisation();
        List<String> auths = Arrays.asList(canRunPulse.getAuthorisation(LicenseHolder.getLicense(), version));
        return auths.contains(LicenseHolder.AUTH_RUN_PULSE);
    }

    /**
     * Configure the list of available authorisations.
     *
     * @param a list of authorisations.
     */
    public void setAuthorisations(List<Authorisation> a)
    {
        this.authorisations = a;
    }

    public void addAuthorisation(Authorisation auth)
    {
        this.authorisations.add(auth);
        refreshAuthorisations();
    }

    /**
     * Required resource.
     *
     * @param eventManager instance.
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * Required resource.
     *
     * @param keyStore instance
     */
    public void setLicenseKeyStore(LicenseKeyStore keyStore)
    {
        this.keyStore = keyStore;
    }
}
