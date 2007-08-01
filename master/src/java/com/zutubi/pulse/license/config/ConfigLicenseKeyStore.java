package com.zutubi.pulse.license.config;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.TypeAdapter;
import com.zutubi.prototype.config.TypeListener;
import com.zutubi.pulse.license.AbstractLicenseKeyStore;
import com.zutubi.pulse.license.LicenseException;

/**
 *
 *
 */
public class ConfigLicenseKeyStore extends AbstractLicenseKeyStore
{
    private ConfigurationProvider configurationProvider;

    public void init()
    {
        TypeListener<LicenseConfiguration> listener = new TypeAdapter<LicenseConfiguration>(LicenseConfiguration.class)
        {
            public void postSave(LicenseConfiguration instance)
            {
                notifyListeners();
            }
        };
        listener.register(configurationProvider);
    }

    public String getKey()
    {
        LicenseConfiguration licenseConfiguration = configurationProvider.get(LicenseConfiguration.class);
        if (licenseConfiguration != null)
        {
            return licenseConfiguration.getKey();
        }
        return null;
    }

    public void setKey(String licenseKey) throws LicenseException
    {
        LicenseConfiguration licenseConfiguration = configurationProvider.get(LicenseConfiguration.class);
        licenseConfiguration.setKey(licenseKey);
        configurationProvider.save(licenseConfiguration);
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
