package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import com.zutubi.validation.annotations.Required;

import java.util.Arrays;

/**
 */
@SymbolicName("zutubi.emailConfigurationCheckHandler")
public class EmailConfigurationCheckHandler extends AbstractConfigurationCheckHandler<EmailConfiguration>
{
    @Required
    private String emailAddress;

    public void test(EmailConfiguration configuration) throws Exception
    {
        EmailContactConfiguration.sendMail(Arrays.asList(emailAddress), configuration, "Test Email", "text/plain", "Welcome to Zutubi Pulse!");
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }
}
