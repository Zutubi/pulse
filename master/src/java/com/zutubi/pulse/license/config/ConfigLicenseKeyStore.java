package com.zutubi.pulse.license.config;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.TypeAdapter;
import com.zutubi.prototype.config.TypeListener;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.license.AbstractLicenseKeyStore;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.Event;

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
        MutableRecord licenseRecord = configurationTemplateManager.getRecord(LICENSE_PATH).copy(false);
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
