package com.cinnamonbob.model;

import com.cinnamonbob.BobServer;
import com.cinnamonbob.bootstrap.ApplicationConfiguration;
import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.renderer.BuildResultRenderer;
import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.util.TextUtils;

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

    public EmailContactPoint()
    {
    }

    public EmailContactPoint(String email)
    {
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

    /* (non-Javadoc)
    * @see com.cinnamonbob.core.ContactPoint#notify(com.cinnamonbob.core.model.RecipeResult)
    */
    public void notify(BuildResult result)
    {
        ApplicationConfiguration config = ConfigUtils.getManager().getAppConfig();
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
        renderer.render(BobServer.getHostURL(), result, BuildResultRenderer.TYPE_PLAIN, w);
        return w.toString();
    }

    private void sendMail(String subject, String body, final ApplicationConfiguration config)
    {
        if (config.getSmtpHost() == null)
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
            msg.setText(body);
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
