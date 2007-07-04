package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.validation.annotations.Required;

/**
 */
@SymbolicName("zutubi.emailConfigurationCheckHandler")
public class EmailConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<EmailConfiguration>
{
    @Required
    private String emailAddress;

    public void test(EmailConfiguration configuration) throws Exception
    {
        EmailContactPoint.sendMail(emailAddress, configuration, "Test Email", "text/plain", "Welcome to Zutubi Pulse!");
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
