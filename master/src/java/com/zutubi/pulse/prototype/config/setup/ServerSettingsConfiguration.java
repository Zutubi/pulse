package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.prototype.config.admin.EmailConfiguration;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Url;

/**
 *
 *
 */
@SymbolicName("zutubi.serverSettingsConfig")
@ConfigurationCheck("ServerSettingsConfigurationCheckHandler")
@Form(fieldOrder = {"baseUrl", "host", "ssl", "from", "username", "password", "subjectPrefix", "customPort", "port"})
public class ServerSettingsConfiguration extends EmailConfiguration
{
    @Url @Required
    private String baseUrl;

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }
}
