package com.cinnamonbob.core;

import nu.xom.Element;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * A point for contacting a user via email.
 */
public class EmailContactPoint implements ContactPoint
{
    private static final Logger LOG = Logger.getLogger(EmailContactPoint.class.getName());
    
    private static final String CONFIG_ELEMENT_ADDRESS = "address";
    
    private Bob             theBuilder;
    private String          name;
    private InternetAddress address;
    
    
    public EmailContactPoint(Bob theBuilder, String name, String filename, Element element) throws ConfigException
    {
        this.theBuilder = theBuilder;
        this.name = name;
        loadConfig(filename, element);
    }

    /* (non-Javadoc)
     * @see com.cinnamonbob.core.ContactPoint#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see com.cinnamonbob.core.ContactPoint#notify(com.cinnamonbob.core.BuildResult)
     */
    public void notify(BuildResult result)
    {
        String subject = "[CiB] " + result.getProjectName() + ": Build " + Integer.toString(result.getId()) + (result.succeeded() ? " succeeded" : " failed");
//        System.out.println(renderResult(result));
        sendMail(subject, renderResult(result));
    }

    
    private String renderResult(BuildResult result)
    {
        StringWriter w = new StringWriter();
        // TODO renderer should come from elsewhere
        VelocityBuildResultRenderer renderer = new VelocityBuildResultRenderer(theBuilder);
        renderer.render(result, BuildResultRenderer.TYPE_PLAIN, w);
        return  w.toString();
    }
    
    private void sendMail(String subject, String body)
    {
        SMTPService smtp = (SMTPService)theBuilder.lookupService(SMTPService.SERVICE_NAME);
        
        if(smtp == null)
        {
            // TODO detect this badness in config somehow
            LOG.warning("Could not locate SMTP service to send email notifications.");
            return;
        }
        
        try
        {
            Session session = smtp.getSession();
            
            Message msg = new MimeMessage(session);
            msg.setFrom(smtp.getFromAddress());
            msg.setRecipient(Message.RecipientType.TO, address);
            msg.setSubject(subject);
            msg.setText(body);
            msg.setHeader("X-Mailer", "Project-Cinnamon");
            msg.setSentDate(new Date());
            
            Transport.send(msg);
            
            System.out.println("Message sent OK.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    private void loadConfig(String filename, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(filename, element, Arrays.asList(CONFIG_ELEMENT_ADDRESS));
        
        for(Element current: elements)
        {
            String elementName = current.getLocalName();
                
            if(elementName.equals(CONFIG_ELEMENT_ADDRESS))
            {
                loadAddress(filename, current);
            }
            else
            {
                assert(false);
            }
        }        
    }

    
    private void loadAddress(String filename, Element element) throws ConfigException
    {
        String addressString = XMLConfigUtils.getElementText(filename, element);
        
        try
        {
            this.address = new InternetAddress(addressString);
        }
        catch(AddressException e)
        {
            throw new ConfigException(filename, "Email contact point '" + name + "' has invalid address '" + addressString + "': " + e.getMessage());
        }
    }
}
