package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.model.NotificationException;
import com.zutubi.pulse.master.notifications.NotificationAttachment;
import com.zutubi.pulse.master.notifications.email.EmailService;
import com.zutubi.pulse.master.notifications.renderer.RenderedResult;
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

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.List;

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
    private EmailService emailService;

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

    @Override
    public boolean supportsAttachments()
    {
        return true;
    }

    public void notify(RenderedResult rendered, List<NotificationAttachment> attachments) throws Exception
    {
        EmailConfiguration config = configurationProvider.get(EmailConfiguration.class);

        if (!StringUtils.stringSet(config.getHost()))
        {
            LOG.severe(NO_SMTP_HOST_ERROR);
            throw new NotificationException(NO_SMTP_HOST_ERROR);
        }

        try
        {
            MimeMultipart message = new MimeMultipart();
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(rendered.getContent(), rendered.getMimeType());
            message.addBodyPart(bodyPart);
            for (NotificationAttachment attachment: attachments)
            {
                message.addBodyPart(attachment.asBodyPart());
            }
            
            emailService.sendMail(Arrays.asList(getAddress()), rendered.getSubject(), message, config, true);
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

    public void setEmailService(EmailService emailService)
    {
        this.emailService = emailService;
    }
}
