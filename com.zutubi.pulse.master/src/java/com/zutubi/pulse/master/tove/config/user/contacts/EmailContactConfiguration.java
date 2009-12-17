package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.NotificationException;
import com.zutubi.pulse.master.notifications.email.DefaultEmailService;
import com.zutubi.pulse.master.notifications.email.EmailService;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;

import java.util.Arrays;

/**
 */
@SymbolicName("zutubi.emailContactConfig")
@Form(fieldOrder = {"name", "address"})
@Classification(single = "email")
@Wire
public class EmailContactConfiguration extends ContactConfiguration
{
    private static final Logger LOG = Logger.getLogger(EmailContactConfiguration.class);

    private static final String NO_SMTP_HOST_ERROR = "Unable to deliver email: SMTP host not configured.";
    
    @Required
    @Email
    private String address;

    private ConfigurationProvider configurationProvider;
    private EmailService emailService = new DefaultEmailService();

    public EmailContactConfiguration()
    {
    }

    public EmailContactConfiguration(String name, String address)
    {
        setName(name);
        setAddress(address);
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getUid()
    {
        return getAddress();
    }

    public void notify(BuildResult result, String subject, String rendered, String mimeType) throws Exception
    {
        EmailConfiguration config = configurationProvider.get(EmailConfiguration.class);

        if (!StringUtils.stringSet(config.getHost()))
        {
            LOG.severe(NO_SMTP_HOST_ERROR);
            throw new NotificationException(NO_SMTP_HOST_ERROR);
        }

        try
        {
            emailService.sendMail(Arrays.asList(getAddress()), subject, mimeType, rendered, config);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to send email to address '" + getAddress() + "': " + e.getMessage(), e);
            throw new NotificationException("Unable to send email to address '" + getAddress() + "': " + e.getMessage() + " (check the address and/or the SMTP server configuration)");
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
