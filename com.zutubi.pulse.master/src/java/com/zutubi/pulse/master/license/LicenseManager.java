package com.zutubi.pulse.master.license;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.license.authorisation.Authorisation;
import com.zutubi.pulse.master.license.events.LicenseUpdateEvent;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.events.InsertEvent;
import com.zutubi.tove.events.ConfigurationSystemEventListener;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The License manager handles all things license related.
 */
public class LicenseManager
{
    private static final Messages I18N = Messages.getInstance(LicenseManager.class);
    private static final Logger LOG = Logger.getLogger(LicenseManager.class);

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
        // check that the license key is valid.  Decode will throw an exception
        // or return null if it is not.
        LicenseDecoder decoder = new LicenseDecoder();
        if (decoder.decode(newKey.getBytes()) == null)
        {
            throw new LicenseException(I18N.format("invalid.key"));
        }

        // persist the license key
        keyStore.setKey(newKey);
    }

    /**
     * Initialise the license manager.
     */
    public void init()
    {
        eventManager.register(new ConfigurationSystemEventListener()
        {
            @Override
            public void configurationEventSystemStarted()
            {
                eventManager.register(new LicenseEnforcingListener());
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
                LOG.warning(I18N.format("invalid.key"), e);
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
        synchronized (LicenseHolder.class)
        {
            List<String> newAuthorisations = new LinkedList<String>();
            
            License license = LicenseHolder.getLicense();
            for (Authorisation auth : authorisations)
            {
                newAuthorisations.addAll(Arrays.asList(auth.getAuthorisation(license)));
            }
            LicenseHolder.setAuthorizations(newAuthorisations);
        }
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
                    throw new LicenseException(I18N.format("add.project.limit.exceeded"));
                }
            }
            else if(instance instanceof AgentConfiguration)
            {
                if(!LicenseHolder.hasAuthorization(LicenseHolder.AUTH_ADD_AGENT))
                {
                    throw new LicenseException(I18N.format("add.agent.limit.exceeded"));
                }
            }
            else if(instance instanceof UserConfiguration)
            {
                if(!LicenseHolder.hasAuthorization(LicenseHolder.AUTH_ADD_USER))
                {
                    throw new LicenseException(I18N.format("add.user.limit.exceeded"));
                }
            }
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{InsertEvent.class};
        }
    }
}
