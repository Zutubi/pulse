package com.zutubi.pulse.model;

import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.util.logging.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
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

    private static final String PROPERTY_TYPE = "type";

    public EmailContactPoint()
    {
        setType(BuildResultRenderer.TYPE_HTML);
    }

    public EmailContactPoint(String email)
    {
        this();
        setEmail(email);
    }

    public String getEmail()
    {
        return getUid();
    }

    public void setEmail(String email)
    {
        setUid(email);
    }

    public String getType()
    {
        return (String)getProperties().get(PROPERTY_TYPE);
    }

    public void setType(String type)
    {
        getProperties().put(PROPERTY_TYPE, type);
    }

    /* (non-Javadoc)
    * @see com.zutubi.pulse.core.ContactPoint#notify(com.zutubi.pulse.core.model.RecipeResult)
    */
    public void notify(BuildResult result)
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
        sendMail(subject, renderResult(result), config);
    }

    private String renderResult(BuildResult result)
    {
        StringWriter w = new StringWriter();
        BuildResultRenderer renderer = (BuildResultRenderer) ComponentContext.getBean("buildResultRenderer");
        BuildManager buildManager = (BuildManager) ComponentContext.getBean("buildManager");
        MasterConfigurationManager configManager = lookupConfigManager();
        renderer.render(configManager.getAppConfig().getBaseUrl(), result, buildManager.getChangesForBuild(result), getType(), w);
        return w.toString();
    }

    private MasterConfigurationManager lookupConfigManager()
    {
        return (MasterConfigurationManager) ComponentContext.getBean("configurationManager");
    }

    private void sendMail(String subject, String body, final MasterConfiguration config)
    {
        if (!TextUtils.stringSet(config.getSmtpHost()))
        {
            LOG.severe("Unable to deliver mail to contact point: SMTP host not configured.");
            return;
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
            msg.setContent(body, "text/" + getType());
            msg.setHeader("X-Mailer", "Project-Cinnamon");
            msg.setSentDate(new Date());

            Transport.send(msg);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to send email", e);
        }
    }


}
