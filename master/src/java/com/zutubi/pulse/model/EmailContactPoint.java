package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
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
        MasterConfiguration config = lookupConfigManager().getAppConfig();

        if (!TextUtils.stringSet(config.getSmtpHost()))
        {
            LOG.severe(NO_SMTP_HOST_ERROR);
            throw new NotificationException(NO_SMTP_HOST_ERROR);
        }

        String prefix = config.getSmtpPrefix();

        if (prefix == null)
        {
            prefix = "";
        }
        else if (prefix.length() > 0)
        {
            prefix += " ";
        }

        try
        {
            sendMail(getEmail(), prefix + subject, mimeType, rendered, config.getSmtpHost(), config.getSmtpPort(), config.getSmtpSSL(), config.getSmtpUsername(), config.getSmtpPassword(), config.getSmtpFrom());
        }
        catch (Exception e)
        {
            LOG.warning("Unable to send email to address '" + getEmail() + "': " + e.getMessage(), e);
            throw new NotificationException("Unable to send email to address '" + getEmail() + "': " + e.getMessage() + " (check the address and/or the SMTP server configuration)");
        }
    }

    private MasterConfigurationManager lookupConfigManager()
    {
        return (MasterConfigurationManager) ComponentContext.getBean("configurationManager");
    }

    public static void sendMail(String email, String subject, String mimeType, String body, String host, int port, boolean ssl, final String username, final String password, String from) throws Exception
    {
        Properties properties = (Properties) System.getProperties().clone();
        if(ssl)
        {
            properties.put(SMTPS_HOST_PROPERTY, host);
        }
        else
        {
            properties.put(SMTP_HOST_PROPERTY, host);
        }

        if(port > 0)
        {
            if(ssl)
            {
                properties.put(SMTPS_PORT_PROPERTY, Integer.toString(port));
            }
            else
            {
                properties.put(SMTP_PORT_PROPERTY, Integer.toString(port));
            }
        }

//        properties.put("mail.smtp.starttls.enable","true");

        Authenticator authenticator = null;
        if (TextUtils.stringSet(username))
        {
            if(ssl)
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
                    return new PasswordAuthentication(username, password);
                }
            };
        }

        Session session = Session.getInstance(properties, authenticator);

        Message msg = new MimeMessage(session);

        if (from != null)
        {
            msg.setFrom(new InternetAddress(from));
        }

        InternetAddress toAddress = new InternetAddress(email);
        msg.setRecipient(Message.RecipientType.TO, toAddress);
        msg.setSubject(subject);
        msg.setContent(body, mimeType);
        msg.setHeader("X-Mailer", "Zutubi-Pulse");
        msg.setSentDate(new Date());

        Transport transport = session.getTransport(ssl ? "smtps" : "smtp");
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
