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
        String prefix = config.getSmtpPrefix();

        if (prefix == null)
        {
            prefix = "";
        }
        else if (prefix.length() > 0)
        {
            prefix += " ";
        }

        String subject = prefix + result.getProject().getName() + ": build " + Long.toString(result.getNumber()) + ": " + result.getState().getPrettyString();
        sendMail(subject, mimeType, rendered, config);
    }

    private MasterConfigurationManager lookupConfigManager()
    {
        return (MasterConfigurationManager) ComponentContext.getBean("configurationManager");
    }

    private void sendMail(String subject, String mimeType, String body, final MasterConfiguration config) throws Exception
    {
        if (!TextUtils.stringSet(config.getSmtpHost()))
        {
            LOG.severe(NO_SMTP_HOST_ERROR);
            throw new NotificationException(NO_SMTP_HOST_ERROR);
        }

        Properties properties = (Properties) System.getProperties().clone();
        properties.put(SMTP_HOST_PROPERTY, config.getSmtpHost());

        Authenticator authenticator = null;
        if (TextUtils.stringSet(config.getSmtpUsername()))
        {
            properties.put(SMTP_AUTH_PROPERTY, "true");
            authenticator = new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
                }
            };
        }

        Session session = Session.getInstance(properties, authenticator);

        try
        {
            Message msg = new MimeMessage(session);

            if (config.getSmtpFrom() != null)
            {
                String fromAddress = config.getSmtpFrom();
                msg.setFrom(new InternetAddress(fromAddress));
            }

            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(getEmail()));
            msg.setSubject(subject);
            msg.setContent(body, "text/" + mimeType);
            msg.setHeader("X-Mailer", "Zutubi-Pulse");
            msg.setSentDate(new Date());

            Transport.send(msg);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to send email to address '" + getEmail() + "': " + e.getMessage(), e);
            throw e;
        }
    }


}
