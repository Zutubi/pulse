package com.zutubi.pulse.license;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.bootstrap.DataResolver;
import com.zutubi.pulse.license.authorisation.Authorisation;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.DataDirectoryChangedEvent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class LicenseManager
{
    private EventManager eventManager;

    private DataResolver resolver;

    private List<Authorisation> authorisations = new LinkedList<Authorisation>();

    public void updateLicenseKey(String newKey) throws LicenseException
    {
        Data data = resolver.getData();
        data.updateLicenseKey(newKey);

        // refresh the authorisations, now that we have a new license.
        LicenseHolder.setLicense(data.getLicense());
        refreshAuthorisations();
    }

    public void init()
    {
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
        // load the license from disk.
        Data data = resolver.getData();
        if (data != null)
        {
            LicenseHolder.setLicense(data.getLicense());
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
     * Required resource.
     *
     * @param resolver
     */
    public void setResolver(DataResolver resolver)
    {
        this.resolver = resolver;
    }

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
}
