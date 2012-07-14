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

import static java.util.Arrays.asList;

/**
 * A default implementation of {@link EmailService} that sends mails
 * synchronously using the JavaMail APIs.
 */
public class DefaultEmailService implements EmailService
{
    private static final Logger LOG = Logger.getLogger(DefaultEmailService.class);

    private static final String PROPERTY_SEND_PARTIAL = "mail.smtp.sendpartial";
    private static final String PROPERTY_TRANSPORT_PROTOCOL = "mail.transport.protocol";
    private static final String PROTOCOL_SMTP = "smtp";
    private static final String PROTOCOL_SMTPS = "smtps";

    private static final String PROPERTY_HOST = "host";
    private static final String PROPERTY_AUTH = "auth";
    private static final String PROPERTY_PORT = "port";
    private static final String PROPERTY_LOCALHOST = "localhost";

    private static final Address[] EMPTY_ADDRESSES = new Address[0];

    private EmailConfiguration sharedConfig;
    private Session sharedSession;
    private Transport sharedTransport;

    public synchronized void sendMail(Collection<String> recipients, String subject, Multipart message, final EmailConfiguration config, boolean reuseSession) throws MessagingException
    {
        subject = addSubjectPrefix(config, subject);

        int attempts = 0;
        int retryLimit = reuseSession ? 3 : 1;
        boolean sent = false;
        while (!sent)
        {
            attempts++;

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

            Message msg = createMessage(recipients, subject, message, config, session);

            try
            {
                if (!transport.isConnected())
                {
                    transport.connect();
                }
                msg.saveChanges();
                transport.sendMessage(msg, msg.getAllRecipients());
                sent = true;
            }
            catch (SendFailedException e)
            {
                if (safeAddresses(e.getValidSentAddresses()).length > 0)
                {
                    // We managed to send to some addresses, just report those that failed.
                    sent = true;
                    LOG.warning("Mail '" + subject + "' only partially sent.  Not sent to valid addresses " +
                            asList(safeAddresses(e.getValidUnsentAddresses())) + " nor invalid addresses " +
                            asList(safeAddresses(e.getInvalidAddresses())) + ".");
                }
                else
                {
                    handleError(reuseSession, attempts == retryLimit, e);
                }
            }
            catch (MessagingException e)
            {
                handleError(reuseSession, attempts == retryLimit, e);
            }
            finally
            {
                if (!reuseSession && transport != null)
                {
                    safeClose(transport);
                }
            }
        }
    }

    private Address[] safeAddresses(Address[] addresses)
    {
        return addresses == null ? EMPTY_ADDRESSES : addresses;
    }

    private void handleError(boolean reuseSession, boolean retriesExhausted, MessagingException e) throws MessagingException
    {
        if (reuseSession)
        {
            cleanupSession();
        }

        if (retriesExhausted)
        {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    private void ensureSharedSession(EmailConfiguration config) throws NoSuchProviderException
    {
        if (!config.isEquivalentTo(sharedConfig))
        {
            cleanupSession();

            sharedConfig = config;
            sharedSession = getSession(config);
            sharedTransport = sharedSession.getTransport();
        }
    }

    private void cleanupSession()
    {
        if (sharedTransport != null)
        {
            safeClose(sharedTransport);

            sharedConfig = null;
            sharedSession = null;
            sharedTransport = null;
        }
    }

    private void safeClose(Transport transport)
    {
        try
        {
            transport.close();
        }
        catch (MessagingException e)
        {
            LOG.info(e);
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
        properties.put(PROPERTY_SEND_PARTIAL, true);
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

    private Message createMessage(Collection<String> recipients, String subject, Multipart message, EmailConfiguration config, Session session) throws MessagingException
    {
        Message msg = new MimeMessage(session);
        if (StringUtils.stringSet(config.getFrom()))
        {
            msg.setFrom(new InternetAddress(config.getFrom()));
        }

        for (String recipient: recipients)
        {
            try
            {
                InternetAddress toAddress = new InternetAddress(recipient);
                msg.addRecipient(Message.RecipientType.TO, toAddress);
            }
            catch (MessagingException e)
            {
                LOG.warning("Unable to add recipient '" + recipient + "' to email '" + subject + "', dropping this recipient.", e);
            }
        }

        msg.setSubject(subject);
        msg.setContent(message);
        msg.setHeader("X-Mailer", "Zutubi-Pulse");
        msg.setSentDate(new Date());
        return msg;
    }
}
