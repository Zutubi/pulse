package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.prototype.config.user.contacts.EmailContactConfiguration;

import javax.mail.MessagingException;

/**
 * Server settings page of the setup wizard.
 */
@SymbolicName("zutubi.serverSettingsConfigurationCheckHandler")
public class ServerSettingsConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<ServerSettingsConfiguration>
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

    public void test(ServerSettingsConfiguration configuration) throws MessagingException
    {
        EmailContactConfiguration.sendMail(testAddress, configuration, "Test Email", "text/plain", "Welcome to Zutubi Pulse!");
    }
}
