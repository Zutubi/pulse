package com.zutubi.pulse.master.license.config;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.DataDirectoryChangedEvent;
import com.zutubi.pulse.master.license.AbstractLicenseKeyStore;
import com.zutubi.pulse.master.license.LicenseException;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.events.PostSaveEvent;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

/**
 * License key store backed by the normal configuration system.
 */
public class ConfigLicenseKeyStore extends AbstractLicenseKeyStore
{
    private static final String LICENSE_PATH = PathUtils.getPath(GlobalConfiguration.SCOPE_NAME, "license");
    private static final String LICENSE_PROPERTY = "key";

    private EventManager eventManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public void init()
    {
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event event)
            {
                if (((PostSaveEvent)event).getInstance() instanceof LicenseConfiguration)
                {
                    notifyListeners();
                }
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{PostSaveEvent.class};
            }
        });

        // We know that the records are stored somewhere in the data directory.
        // So, if the data directory is changed, then the underlying license is
        // also changed.
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event evt)
            {
                notifyListeners();
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{DataDirectoryChangedEvent.class};
            }
        });
    }

    public String getKey()
    {
        // Note that we talk directly in records as we are accessed early in
        // setup before instances are available.
        Record licenseRecord = configurationTemplateManager.getRecord(LICENSE_PATH);
        if (licenseRecord != null)
        {
            return (String) licenseRecord.get(LICENSE_PROPERTY);
        }
        return null;
    }

    public void setKey(String licenseKey) throws LicenseException
    {
        MutableRecord licenseRecord = configurationTemplateManager.getRecord(LICENSE_PATH).copy(false, true);
        licenseRecord.put(LICENSE_PROPERTY, licenseKey);
        configurationTemplateManager.saveRecord(LICENSE_PATH, licenseRecord);
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
