package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.annotation.TextArea;
import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.pulse.model.EmailContactPoint;

import javax.mail.MessagingException;

/**
 */
@SymbolicName("internal.emailConfigurationCheckHandler")
public class EmailConfigurationCheckHandler implements ConfigurationCheckHandler<EmailConfiguration>
{
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
