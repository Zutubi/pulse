package com.cinnamonbob.model;

import com.cinnamonbob.bootstrap.ConfigUtils;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.renderer.BuildResultRenderer;
import com.cinnamonbob.core.renderer.VelocityBuildResultRenderer;

import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * 
 *
 */
public class EmailContactPoint extends ContactPoint
{
    private static final Logger LOG = Logger.getLogger(EmailContactPoint.class.getName());

    private static final String SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String SMTP_FROM_PROPERTY = "mail.smtp.from";

    private BuildResultRenderer renderer;
    
    public EmailContactPoint()
    {
        renderer = new VelocityBuildResultRenderer();
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
    * @see com.cinnamonbob.core.ContactPoint#notify(com.cinnamonbob.model.BuildResult)
    */
    public void notify(Project project, BuildResult result)
    {
        String subject = "[CiB] " + result.getProjectName() + ": build " + Long.toString(result.getNumber()) + ": " + result.getState().getPrettyString();
        sendMail(subject, renderResult(project, result));
    }
    
    private String renderResult(Project project, BuildResult result)
    {
        StringWriter w = new StringWriter();
        renderer.render(project, result, BuildResultRenderer.TYPE_PLAIN, w);
        return  w.toString();
    }
    
    private void sendMail(String subject, String body)
    {
        ConfigurationManager config     = ConfigUtils.getManager();
        Properties           properties = System.getProperties();
        
        if(!config.hasProperty(SMTP_HOST_PROPERTY))
        {
            LOG.severe("Unable to deliver mail to contact point: SMTP host not configured.");
            return;
        }
        
        properties.put(SMTP_HOST_PROPERTY, config.lookupProperty(SMTP_HOST_PROPERTY));
        
        Session session = Session.getDefaultInstance(properties, null);
        
        try
        {
            Message msg = new MimeMessage(session);
            
            if(config.hasProperty(SMTP_FROM_PROPERTY))
            {
                String fromAddress = config.lookupProperty(SMTP_FROM_PROPERTY);
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
            LOG.log(Level.WARNING, "Unable to send email", e);
        }
    }
    
    
}
