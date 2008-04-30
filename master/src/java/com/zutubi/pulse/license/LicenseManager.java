package com.zutubi.pulse.license;

import com.zutubi.prototype.config.events.InsertEvent;
import com.zutubi.pulse.bootstrap.DataResolver;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.events.DataDirectoryChangedEvent;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.system.ConfigurationSystemStartedEvent;
import com.zutubi.pulse.license.authorisation.Authorisation;
import com.zutubi.pulse.license.events.LicenseUpdateEvent;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The License manager handles all things license related.
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

    private DataResolver dataResolver;

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
        decoder.decode(newKey.getBytes());

        // persist the license key
        keyStore.setKey(newKey);
    }

    /**
     * Initialise the license manager.
     */
    public void init()
    {
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                if(evt instanceof DataDirectoryChangedEvent)
                {
                    // the license manager monitors for changes in the system data directory. We 'know' this is
                    // where the license is stored, so if there is a change, we need to refresh.  This SHOULD be reflected
                    // in the licenseKeyStore / licenseManager interaction somehow since it is the license key store that
                    // is using the data directory for storage purposes.
                    refresh();
                }
                else
                {
                    // Now the config system is up, we can start enforcing the license.
                    eventManager.register(new LicenseEnforcingListener());
                }
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{DataDirectoryChangedEvent.class, ConfigurationSystemStartedEvent.class};
            }
        });

        keyStore.register(new LicenseKeyStoreListener()
        {
            public void keyChanged()
            {
                refresh();
            }
        });

        refresh();
    }

    private void refresh()
    {
        String key = keyStore.getKey();
        License license = null;

        if (key != null)
        {
            try
            {
                LicenseDecoder decoder = new LicenseDecoder();
                license = decoder.decode(key.getBytes());
            }
            catch (LicenseException e)
            {
                // license key is invalid.
                LOG.warning("Failed to decode the configured license key.", e);
                license = null;
            }
        }

        LicenseHolder.setLicense(license);

        // refresh the supported authorisations.
        refreshAuthorisations();
        eventManager.publish(new LicenseUpdateEvent(license));
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

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setLicenseKeyStore(LicenseKeyStore keyStore)
    {
        this.keyStore = keyStore;
    }

    public void setDataResolver(DataResolver dataResolver)
    {
        this.dataResolver = dataResolver;
    }

    /**
     * This listener enforces limits on configured entities.  We hook it up
     * once the configuration system is ready.
     */
    private static class LicenseEnforcingListener implements EventListener
    {
        public void handleEvent(Event event)
        {
            InsertEvent insertEvent = (InsertEvent) event;
            Configuration instance = insertEvent.getInstance();
            if(instance instanceof ProjectConfiguration)
            {
                if(!LicenseHolder.hasAuthorization(LicenseHolder.AUTH_ADD_PROJECT))
                {
                    throw new LicenseException("Unable to add project: license limit exceeded");
                }
            }
            else if(instance instanceof AgentConfiguration)
            {
                if(!LicenseHolder.hasAuthorization(LicenseHolder.AUTH_ADD_AGENT))
                {
                    throw new LicenseException("Unable to add agent: license limit exceeded");
                }
            }
            else if(instance instanceof UserConfiguration)
            {
                if(!LicenseHolder.hasAuthorization(LicenseHolder.AUTH_ADD_USER))
                {
                    throw new LicenseException("Unable to add user: license limit exceeded");
                }
            }
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{InsertEvent.class};
        }
    }
}
