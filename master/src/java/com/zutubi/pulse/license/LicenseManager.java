package com.zutubi.pulse.license;

import com.zutubi.prototype.config.ConfigurationEventListener;
import com.zutubi.prototype.config.PathPredicate;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.events.DataDirectoryChangedEvent;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.FilteringListener;
import com.zutubi.pulse.license.authorisation.Authorisation;
import com.zutubi.pulse.license.authorisation.CanRunPulseAuthorisation;
import com.zutubi.pulse.prototype.config.admin.LicenseConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The License manager handles all things license related.
 */
public class LicenseManager implements ConfigurationEventListener
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

        //FIXME: it would be nice not to have to 'go behind the back' of the configuration system like this
        //       because the system is not yet avialable.  Also, the real solution is probably to support
        //       storage of data externally from the configuration system, so that we dont have the data in two locations.
        FilteringListener filter = new FilteringListener(new PathPredicate(true, "/global/*"), new EventListener()
        {
            // we need this state check to prevent the listeners updating of the license from triggering
            // this listener over and over.
            private boolean updatingLicense = false;

            public void handleEvent(Event evt)
            {
                if (evt instanceof PostSaveEvent)
                {
                    PostSaveEvent psEvt = (PostSaveEvent) evt;
                    if (psEvt.getNewInstance() instanceof LicenseConfiguration)
                    {
                        if (updatingLicense)
                        {
                            return;
                        }
                        try
                        {
                            updatingLicense = true;
                            LicenseConfiguration config = (LicenseConfiguration) psEvt.getNewInstance();
                            installLicense(config.getKey());

                            License license = LicenseHolder.getLicense();
                            config.setName(license.getHolder());
                            config.setType(license.getType().toString());

                            // now we also need to save the change.
                            ConfigurationProvider provider = ComponentContext.getBean("configurationProvider");
                            provider.save(config.getConfigurationPath(), config);
                        }
                        finally
                        {
                            updatingLicense = false;
                        }
                    }
                }
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{ConfigurationEvent.class};
            }
        });
        eventManager.register(filter);

        // also, on system started, we need to sync the existing license details with those of the configuration system. 

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

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if (event instanceof PostSaveEvent)
        {
            LicenseConfiguration config = (LicenseConfiguration) ((PostSaveEvent) event).getNewInstance();
            installLicense(config.getKey());

            License license = LicenseHolder.getLicense();
            config.setName(license.getHolder());
        }
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
