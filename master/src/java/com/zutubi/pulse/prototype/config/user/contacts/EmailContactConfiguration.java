package com.zutubi.pulse.prototype.config.user.contacts;

import com.opensymphony.util.TextUtils;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.NotificationException;
import com.zutubi.pulse.prototype.config.admin.EmailConfiguration;
import com.zutubi.util.logging.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 */
@SymbolicName("zutubi.emailContactConfig")
@Form(fieldOrder = {"name", "address"})
public class EmailContactConfiguration extends ContactConfiguration
{
    private static final Logger LOG = Logger.getLogger(EmailContactConfiguration.class);

    private static final String SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String SMTP_AUTH_PROPERTY = "mail.smtp.auth";
    private static final String SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String SMTP_LOCALHOST_PROPERTY = "mail.smtp.localhost";
    private static final String SMTPS_HOST_PROPERTY = "mail.smtps.host";
    private static final String SMTPS_AUTH_PROPERTY = "mail.smtps.auth";
    private static final String SMTPS_PORT_PROPERTY = "mail.smtps.port";
    private static final String SMTPS_LOCALHOST_PROPERTY = "mail.smtps.localhost";

    private static final String NO_SMTP_HOST_ERROR = "Unable to deliver email: SMTP host not configured.";

    private String address;

    private ConfigurationProvider configurationProvider;

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

    public void internalNotify(BuildResult result, String subject, String rendered, String mimeType) throws Exception
    {
        EmailConfiguration config = configurationProvider.get(EmailConfiguration.class);

        if (!TextUtils.stringSet(config.getHost()))
        {
            LOG.severe(NO_SMTP_HOST_ERROR);
            throw new NotificationException(NO_SMTP_HOST_ERROR);
        }

        try
        {
            sendMail(Arrays.asList(getAddress()), config, subject, mimeType, rendered);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to send email to address '" + getAddress() + "': " + e.getMessage(), e);
            throw new NotificationException("Unable to send email to address '" + getAddress() + "': " + e.getMessage() + " (check the address and/or the SMTP server configuration)");
        }
    }

    public static void sendMail(List<String> emails, final EmailConfiguration config, String subject, String mimeType, String message) throws MessagingException
    {
        String prefix = config.getSubjectPrefix();
        if (TextUtils.stringSet(prefix))
        {
            subject = prefix + " " + subject;
        }

        Properties properties = (Properties) System.getProperties().clone();
        if(config.getSsl())
        {
            properties.put(SMTPS_HOST_PROPERTY, config.getHost());
        }
        else
        {
            properties.put(SMTP_HOST_PROPERTY, config.getHost());
        }

        if(config.isCustomPort())
        {
            if(config.getSsl())
            {
                properties.put(SMTPS_PORT_PROPERTY, Integer.toString(config.getPort()));
            }
            else
            {
                properties.put(SMTP_PORT_PROPERTY, Integer.toString(config.getPort()));
            }
        }

        String localhost = config.getLocalhost();
        if(TextUtils.stringSet(localhost))
        {
            if(config.getSsl())
            {
                properties.put(SMTPS_LOCALHOST_PROPERTY, localhost);
            }
            else
            {
                properties.put(SMTP_LOCALHOST_PROPERTY, localhost);
            }
        }

//        properties.put("mail.smtp.starttls.enable","true");

        Authenticator authenticator = null;
        if (TextUtils.stringSet(config.getUsername()))
        {
            if(config.getSsl())
            {
                properties.put(SMTPS_AUTH_PROPERTY, "true");
            }
            else
            {
                properties.put(SMTP_AUTH_PROPERTY, "true");
            }

            authenticator = new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            };
        }

        Session session = Session.getInstance(properties, authenticator);

        Message msg = new MimeMessage(session);

        if (TextUtils.stringSet(config.getFrom()))
        {
            msg.setFrom(new InternetAddress(config.getFrom()));
        }

        for(String email: emails)
        {
            InternetAddress toAddress = new InternetAddress(email);
            msg.addRecipient(Message.RecipientType.TO, toAddress);
        }

        msg.setSubject(subject);
        msg.setContent(message, mimeType);
        msg.setHeader("X-Mailer", "Zutubi-Pulse");
        msg.setSentDate(new Date());

        Transport transport = session.getTransport(config.getSsl() ? "smtps" : "smtp");
        try
        {
            transport.connect();
            msg.saveChanges();
            transport.sendMessage(msg, msg.getAllRecipients());
        }
        finally
        {
            transport.close();
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
