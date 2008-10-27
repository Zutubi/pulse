package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.validation.annotations.Required;

import javax.mail.MessagingException;
import java.util.Arrays;

/**
 * Server settings page of the setup wizard.
 */
@SymbolicName("zutubi.serverSettingsConfigurationCheckHandler")
public class ServerSettingsConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<ServerSettingsConfiguration>
{
    @Required
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
        EmailContactConfiguration.sendMail(Arrays.asList(testAddress), configuration, "Test Email", "text/plain", "Welcome to Zutubi Pulse!");
    }
}
