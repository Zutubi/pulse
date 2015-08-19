package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Url;

/**
 *
 *
 */
@SymbolicName("zutubi.serverSettingsConfig")
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
