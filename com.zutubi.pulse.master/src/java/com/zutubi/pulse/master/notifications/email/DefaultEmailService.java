/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.notifications.email;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.pulse.servercore.util.background.BackgroundServiceSupport;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.*;

import static java.util.Arrays.asList;

/**
 * A default implementation of {@link EmailService} that sends mails
 * synchronously using the JavaMail APIs.
 */
public class DefaultEmailService extends BackgroundServiceSupport implements EmailService
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

    private static final String PROPERTY_QUEUE_CAPACITY = "pulse.email.queue.capacity";

    private static final Address[] EMPTY_ADDRESSES = new Address[0];

    private EmailConfiguration sharedConfig;
    private Session sharedSession;
    private Transport sharedTransport;

    public DefaultEmailService()
    {
        super("Email", new ThreadPoolExecutor(
                      1,
                      1,
                      0L,
                      TimeUnit.MILLISECONDS,
                      new LinkedBlockingQueue<Runnable>(Integer.getInteger(PROPERTY_QUEUE_CAPACITY, 4096)),
                      new RejectedExecutionHandler()
                      {
                          public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor)
                          {
                              throw new PulseRuntimeException("Email queue is full, rejecting email.");
                          }
                      }
              ), true);
    }

    public void sendMail(final Collection<String> recipients, final String subject, final Multipart message, final EmailConfiguration config) throws MessagingException
    {
        MessagingException exception = new SendEmail(recipients, addSubjectPrefix(config, subject), message, config, false).call();
        if (exception != null)
        {
            throw new MessagingException(exception.getMessage(), exception);
        }
    }

    public Future<MessagingException> queueMail(final Collection<String> recipients, final String subject, final Multipart message, final EmailConfiguration config)
    {
        return getExecutorService().submit(new SendEmail(recipients, addSubjectPrefix(config, subject), message, config, true));
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

    private class SendEmail implements Callable<MessagingException>
    {
        private final Collection<String> recipients;
        private final String             subject;
        private final Multipart          message;
        private final EmailConfiguration config;
        private final boolean            reuseSession;

        public SendEmail(Collection<String> recipients, String subject, Multipart message, EmailConfiguration config, boolean reuseSession)
        {
            this.recipients = recipients;
            this.subject = subject;
            this.message = message;
            this.config = config;
            this.reuseSession = reuseSession;
        }

        public MessagingException call()
        {
            int attempts = 0;
            int retryLimit = reuseSession ? 3 : 1;
            boolean sent = false;
            try
            {
                while (!sent)
                {
                    attempts++;
                    sent = attemptSend(subject, attempts == retryLimit);
                }

                return null;
            }
            catch (MessagingException e)
            {
                LOG.severe("Failed to send email '" + subject + "': " + e.getMessage(), e);
                return e;
            }
        }

        private boolean attemptSend(String subject, boolean retriesExhausted) throws MessagingException
        {
            Session session;
            Transport transport;
            boolean sent = false;
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
                    handleError(reuseSession, retriesExhausted, e);
                }
            }
            catch (Throwable t)
            {
                handleError(reuseSession, retriesExhausted, t);
            }
            finally
            {
                if (!reuseSession && transport != null)
                {
                    safeClose(transport);
                }
            }

            return sent;
        }

        private Address[] safeAddresses(Address[] addresses)
        {
            return addresses == null ? EMPTY_ADDRESSES : addresses;
        }

        private void handleError(boolean reuseSession, boolean retriesExhausted, Throwable t) throws MessagingException
        {
            if (reuseSession)
            {
                cleanupSession();
            }

            if (retriesExhausted)
            {
                MessagingException messagingException = new MessagingException(t.getMessage());
                messagingException.initCause(t);
                throw messagingException;
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
}
