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
    
    public SMTPService(String filename, Element element) throws ConfigException
    {
        loadConfig(filename, element);
        properties = System.getProperties();
        properties.put("mail.smtp.host", host);
    }


    private void loadConfig(String filename, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(filename, element, Arrays.asList(CONFIG_ELEMENT_HOST, CONFIG_ELEMENT_PORT, CONFIG_ELEMENT_FROM_ADDRESS));
        for(Element e: elements)
        {
            String elementName = e.getLocalName();
            
            if(elementName.equals(CONFIG_ELEMENT_HOST))
            {
                loadHost(filename, e);
            }
            else if(elementName.equals(CONFIG_ELEMENT_PORT))
            {
                loadPort(filename, e);
            }
            else if(elementName.equals(CONFIG_ELEMENT_FROM_ADDRESS))
            {
                loadFromAddress(filename, e);
            }
            else
            {
                assert(false);
            }            
        }
    }
    
    
    private void loadHost(String filename, Element element) throws ConfigException
    {
        host = XMLConfigUtils.getElementText(filename, element);
    }


    private void loadPort(String filename, Element element) throws ConfigException
    {
        // TODO port not yet specified
        port = XMLConfigUtils.getElementInt(filename, element, 1, Integer.MAX_VALUE);
    }


    private void loadFromAddress(String filename, Element element) throws ConfigException
    {
        String text = XMLConfigUtils.getElementText(filename, element);
        
        try
        {
            fromAddress = new InternetAddress(text);
        }
        catch(AddressException e)
        {
            throw new ConfigException(filename, "Invalid email address specified for SMTP service: " + e.getMessage());
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
