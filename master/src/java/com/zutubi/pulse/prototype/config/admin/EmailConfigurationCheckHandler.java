package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.annotation.TextArea;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.emailConfigurationCheckHandler")
public class EmailConfigurationCheckHandler implements ConfigurationCheckHandler<EmailConfiguration>
{
    private String emailAddress;
    private String message;

    public void test(EmailConfiguration configuration)
    {
        // send a test email to check the email server configuration.
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    @TextArea()
    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
