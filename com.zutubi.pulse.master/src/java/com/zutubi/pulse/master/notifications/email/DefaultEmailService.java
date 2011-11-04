package com.zutubi.pulse.master.notifications.email;

import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(DefaultEmailService.class);

    private static final String PROPERTY_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    private static final String PROTOCOL_SMTP = "smtp";
    private static final String PROTOCOL_SMTPS = "smtps";

    private static final String PROPERTY_HOST = "host";
    private static final String PROPERTY_AUTH = "auth";
    private static final String PROPERTY_PORT = "port";
    private static final String PROPERTY_LOCALHOST = "localhost";

    private EmailConfiguration sharedConfig;
    private Session sharedSession;
    private Transport sharedTransport;

    public synchronized void sendMail(Collection<String> recipients, String subject, String mimeType, String message, final EmailConfiguration config, boolean reuseSession) throws MessagingException
    {
        Session session;
        Transport transport;
        if (reuseSession)
        {
            ensureSharedSession(config);
            session = sharedSession;
            transport = sharedTransport;
        }
        else
        {
            session = getSession(config);
            transport = session.getTransport();
        }

        subject = addSubjectPrefix(config, subject);
        Message msg = createMessage(recipients, subject, mimeType, message, config, session);

        try
        {
            if (!transport.isConnected())
            {
                transport.connect();
            }
            msg.saveChanges();
            transport.sendMessage(msg, msg.getAllRecipients());
        }
        finally
        {
            if (!reuseSession && transport != null)
            {
                transport.close();
            }
        }
    }

    private void ensureSharedSession(EmailConfiguration config) throws NoSuchProviderException
    {
        if (!config.isEquivalentTo(sharedConfig))
        {
            if (sharedTransport != null)
            {
                try
                {
                    sharedTransport.close();
                }
                catch (MessagingException e)
                {
                    LOG.warning(e);
                }
            }

            sharedConfig = config;
            sharedSession = getSession(config);
            sharedTransport = sharedSession.getTransport();
        }
    }

    private String addSubjectPrefix(EmailConfiguration config, String subject)
    {
        String prefix = config.getSubjectPrefix();
        if (StringUtils.stringSet(prefix))
        {
            subject = prefix + " " + subject;
        }
        return subject;
    }

    private Session getSession(final EmailConfiguration config)
    {
        Properties properties = (Properties) System.getProperties().clone();
        properties.put(PROPERTY_TRANSPORT_PROTOCOL, getProtocol(config));
        properties.put(getProperty(config, PROPERTY_HOST), config.getHost());

        if (config.isCustomPort())
        {
            properties.put(getProperty(config, PROPERTY_PORT), Integer.toString(config.getPort()));
        }

        String localhost = config.getLocalhost();
        if (StringUtils.stringSet(localhost))
        {
            properties.put(getProperty(config, PROPERTY_LOCALHOST), localhost);
        }

        Authenticator authenticator = null;
        if (StringUtils.stringSet(config.getUsername()))
        {
            properties.put(getProperty(config, PROPERTY_AUTH), "true");

            authenticator = new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            };
        }

        return Session.getInstance(properties, authenticator);
    }

    private String getProtocol(EmailConfiguration config)
    {
        return config.getSsl() ? PROTOCOL_SMTPS : PROTOCOL_SMTP;
    }

    private String getProperty(EmailConfiguration config, String name)
    {
        return "mail." + getProtocol(config) + "." + name;
    }

    private Message createMessage(Collection<String> recipients, String subject, String mimeType, String message, EmailConfiguration config, Session session) throws MessagingException
    {
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
        return msg;
    }
}
