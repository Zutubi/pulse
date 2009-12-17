package com.zutubi.pulse.master.notifications.email;

import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.util.StringUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

/**
 * A default implementation of {@link EmailService} that sends mails
 * synchronously using the JavaMail APIs.
 */
public class DefaultEmailService implements EmailService
{
    private static final String SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String SMTP_AUTH_PROPERTY = "mail.smtp.auth";
    private static final String SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String SMTP_LOCALHOST_PROPERTY = "mail.smtp.localhost";
    private static final String SMTPS_HOST_PROPERTY = "mail.smtps.host";
    private static final String SMTPS_AUTH_PROPERTY = "mail.smtps.auth";
    private static final String SMTPS_PORT_PROPERTY = "mail.smtps.port";
    private static final String SMTPS_LOCALHOST_PROPERTY = "mail.smtps.localhost";

    public void sendMail(Collection<String> recipients, String subject, String mimeType, String message, final EmailConfiguration config) throws MessagingException
    {
        String prefix = config.getSubjectPrefix();
        if (StringUtils.stringSet(prefix))
        {
            subject = prefix + " " + subject;
        }

        Properties properties = (Properties) System.getProperties().clone();
        if (config.getSsl())
        {
            properties.put(SMTPS_HOST_PROPERTY, config.getHost());
        }
        else
        {
            properties.put(SMTP_HOST_PROPERTY, config.getHost());
        }

        if (config.isCustomPort())
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
        if (StringUtils.stringSet(localhost))
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

        Authenticator authenticator = null;
        if (StringUtils.stringSet(config.getUsername()))
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

        if (StringUtils.stringSet(config.getFrom()))
        {
            msg.setFrom(new InternetAddress(config.getFrom()));
        }

        for (String recipient: recipients)
        {
            InternetAddress toAddress = new InternetAddress(recipient);
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
}
