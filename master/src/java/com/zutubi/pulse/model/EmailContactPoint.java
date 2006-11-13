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

    public void internalNotify(BuildResult result, String rendered, String mimeType) throws Exception
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

        String prelude = result.isPersonal() ? "personal build " : (result.getProject().getName() + ": build ");
        String subject = prefix + prelude + Long.toString(result.getNumber()) + ": " + result.getState().getPrettyString();

        try
        {
            sendMail(getEmail(), subject, mimeType, rendered, config.getSmtpHost(), config.getSmtpUsername(), config.getSmtpPassword(), config.getSmtpFrom());
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

    public static void sendMail(String email, String subject, String mimeType, String body, String host, final String username, final String password, String from) throws Exception
    {
        Properties properties = (Properties) System.getProperties().clone();
        properties.put(SMTP_HOST_PROPERTY, host);

        Authenticator authenticator = null;
        if (TextUtils.stringSet(username))
        {
            properties.put(SMTP_AUTH_PROPERTY, "true");
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

        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        msg.setSubject(subject);
        msg.setContent(body, mimeType);
        msg.setHeader("X-Mailer", "Zutubi-Pulse");
        msg.setSentDate(new Date());

        Transport.send(msg);
    }
}
