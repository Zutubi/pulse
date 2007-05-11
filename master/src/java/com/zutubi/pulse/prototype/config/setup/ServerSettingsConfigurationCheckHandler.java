package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandlerSupport;

/**
 *
 *
 */
@SymbolicName("internal.serverSettingsConfigurationCheckHandler")
public class ServerSettingsConfigurationCheckHandler extends ConfigurationCheckHandlerSupport
{
    private String testAddress;

    public String getTestAddress()
    {
        return testAddress;
    }

    public void setTestAddress(String testAddress)
    {
        this.testAddress = testAddress;
    }

    public void test(Object configuration)
    {

    }
}
