package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.serverSettingsConfigurationCheckHandler")
public class ServerSettingsConfigurationCheckHandler implements ConfigurationCheckHandler
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
