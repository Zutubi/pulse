package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.prototype.config.admin.EmailConfiguration;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 *
 *
 */
public class EmailContactPoint extends ContactPoint
{
    private static final Logger LOG = Logger.getLogger(EmailContactPoint.class);

    private static final String SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String SMTP_AUTH_PROPERTY = "mail.smtp.auth";
    private static final String SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String SMTPS_HOST_PROPERTY = "mail.smtps.host";
    private static final String SMTPS_AUTH_PROPERTY = "mail.smtps.auth";
    private static final String SMTPS_PORT_PROPERTY = "mail.smtps.port";

    private static final String NO_SMTP_HOST_ERROR = "Unable to deliver email: SMTP host not configured.";

    public EmailContactPoint()
    {
    }

    public EmailContactPoint(String email)
    {
        this();
        setEmail(email);
    }

    @Required @Email public String getEmail()
    {
        return getUid();
    }

    public void setEmail(String email)
    {
        setUid(email);
    }

    public String getDefaultTemplate()
    {
        return "html-email";
    }

    public void internalNotify(BuildResult result, String subject, String rendered, String mimeType) throws Exception
    {
        EmailConfiguration config = lookupConfigProvider().get(EmailConfiguration.class);

        if (!TextUtils.stringSet(config.getHost()))
        {
            LOG.severe(NO_SMTP_HOST_ERROR);
            throw new NotificationException(NO_SMTP_HOST_ERROR);
        }

        try
        {
            sendMail(getEmail(), config, subject, mimeType, rendered);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to send email to address '" + getEmail() + "': " + e.getMessage(), e);
            throw new NotificationException("Unable to send email to address '" + getEmail() + "': " + e.getMessage() + " (check the address and/or the SMTP server configuration)");
        }
    }

    private ConfigurationProvider lookupConfigProvider()
    {
        return (ConfigurationProvider) ComponentContext.getBean("configurationProvider");
    }

    public static void sendMail(String address, final EmailConfiguration config, String subject, String mimeType, String message) throws MessagingException
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

        InternetAddress toAddress = new InternetAddress(address);
        msg.setRecipient(Message.RecipientType.TO, toAddress);
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
}
