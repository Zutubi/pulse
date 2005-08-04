package com.cinnamonbob.model;

import com.cinnamonbob.core2.BuildResult;
import com.cinnamonbob.core2.renderer.BuildResultRenderer;
import com.cinnamonbob.core2.renderer.VelocityBuildResultRenderer;

import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

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

    public EmailContactPoint()
    {
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
    * @see com.cinnamonbob.core.ContactPoint#notify(com.cinnamonbob.core.BuildResult)
    */
    public void notify(Project project, BuildResult result)
    {
        String subject = "[CiB] " + result.getProjectName() + ": build " + Long.toString(result.getNumber()) + (result.succeeded() ? " succeeded" : " failed");
        sendMail(subject, renderResult(project, result));
    }
    
    private String renderResult(Project project, BuildResult result)
    {
        StringWriter w = new StringWriter();
        // TODO renderer should come from elsewhere
        VelocityBuildResultRenderer renderer = new VelocityBuildResultRenderer();
        renderer.render(project, result, BuildResultRenderer.TYPE_PLAIN, w);
        return  w.toString();
    }
    
    private void sendMail(String subject, String body)
    {
        // FIXME HAAAAAAAAAAAACCCCCCCCKKKK!!!
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", "smtp.people.net.au");
        
        Session session = Session.getDefaultInstance(properties, null);
        try
        {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("jason.sankey@sensorynetworks.com"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(getEmail()));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setHeader("X-Mailer", "Project-Cinnamon");
            msg.setSentDate(new Date());
              
            Transport.send(msg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

//        SMTPService smtp = (SMTPService)theBuilder.lookupService(SMTPService.SERVICE_NAME);
//        
//        if(smtp == null)
//        {
//            // TODO detect this badness in config somehow
//            LOG.warning("Could not locate SMTP service to send email notifications.");
//            return;
//        }
//        
//        try
//        {
//            Session session = smtp.getSession();
//            
//            Message msg = new MimeMessage(session);
//            msg.setFrom(smtp.getFromAddress());
//            msg.setRecipient(Message.RecipientType.TO, address);
//            msg.setSubject(subject);
//            msg.setText(body);
//            msg.setHeader("X-Mailer", "Project-Cinnamon");
//            msg.setSentDate(new Date());
//            
//            Transport.send(msg);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
    }
    
    
}
