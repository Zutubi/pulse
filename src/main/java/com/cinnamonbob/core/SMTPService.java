package com.cinnamonbob.core;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import nu.xom.Element;


public class SMTPService implements Service
{
    private static final String CONFIG_ELEMENT_HOST         = "host";
    private static final String CONFIG_ELEMENT_PORT         = "port";
    private static final String CONFIG_ELEMENT_FROM_ADDRESS = "from-address";
    
    public static final String SERVICE_NAME = "stmp";
    
    private String host;
    private int    port;
    private InternetAddress fromAddress;
    private Properties properties;
    
    public SMTPService(ConfigContext context, Element element) throws ConfigException
    {
        loadConfig(context, element);
        properties = System.getProperties();
        properties.put("mail.smtp.host", host);
    }


    private void loadConfig(ConfigContext context, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList(CONFIG_ELEMENT_HOST, CONFIG_ELEMENT_PORT, CONFIG_ELEMENT_FROM_ADDRESS));
        for(Element e: elements)
        {
            String elementName = e.getLocalName();
            
            if(elementName.equals(CONFIG_ELEMENT_HOST))
            {
                loadHost(context, e);
            }
            else if(elementName.equals(CONFIG_ELEMENT_PORT))
            {
                loadPort(context, e);
            }
            else if(elementName.equals(CONFIG_ELEMENT_FROM_ADDRESS))
            {
                loadFromAddress(context, e);
            }
            else
            {
                assert(false);
            }            
        }
    }
    
    
    private void loadHost(ConfigContext context, Element element) throws ConfigException
    {
        host = XMLConfigUtils.getElementText(context, element);
    }


    private void loadPort(ConfigContext context, Element element) throws ConfigException
    {
        // TODO port not yet specified
        port = XMLConfigUtils.getElementInt(context, element, 1, Integer.MAX_VALUE);
    }


    private void loadFromAddress(ConfigContext context, Element element) throws ConfigException
    {
        String text = XMLConfigUtils.getElementText(context, element);
        
        try
        {
            fromAddress = new InternetAddress(text);
        }
        catch(AddressException e)
        {
            throw new ConfigException(context.getFilename(), "Invalid email address specified for SMTP service: " + e.getMessage());
        }
    }


    public String getServiceName()
    {
        return SERVICE_NAME;
    }
    
    
    public Session getSession()
    {
        return Session.getDefaultInstance(properties, null);
    }


    public InternetAddress getFromAddress()
    {
        return fromAddress;
    }
}
