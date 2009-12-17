package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.pulse.master.notifications.email.DefaultEmailService;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import com.zutubi.validation.annotations.Required;

import javax.mail.MessagingException;
import java.util.Arrays;

/**
 * Server settings page of the setup wizard.
 */
@SymbolicName("zutubi.serverSettingsConfigurationCheckHandler")
public class ServerSettingsConfigurationCheckHandler extends AbstractConfigurationCheckHandler<ServerSettingsConfiguration>
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
        new DefaultEmailService().sendMail(Arrays.asList(testAddress), "Test Email", "text/plain", "Welcome to Zutubi Pulse!", configuration);
    }
}
