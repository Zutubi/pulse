package com.cinnamonbob.core;

import nu.xom.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A user is a person involved in one or more projects.  Users can subscribe
 * to various projects and define when and how they can be notified of events.
 */
public class User
{
    private static final String CONFIG_ELEMENT_NAME               = "name";
    private static final String CONFIG_ELEMENT_CONTACT_POINTS     = "contact-points";
    private static final String CONFIG_ELEMENT_SUBSCRIPTIONS      = "subscriptions";
    private static final String CONFIG_ELEMENT_SUBSCRIPTION       = "subscription";
    private static final String CONFIG_CONTACT_POINT_NAME         = "name";
    private static final String CONFIG_SUBSCRIPTION_PROJECT       = "project";
    private static final String CONFIG_SUBSCRIPTION_CONTACT_POINT = "contact-point";
    
    
    private Bob theBuilder;
    /**
     * A unique login name for the user.  Should match login used for other
     * tools.
     */
    private String login;
    /**
     * Users real name (in whatever format they prefer).
     */
    private String name;
    /**
     * Contact points for this user.
     */
    private Map<String, ContactPoint> contactPoints;

    
    public User(Bob theBuilder, String login, String filename, Element element) throws ConfigException
    {
        this.theBuilder = theBuilder;
        this.login = login;
        this.contactPoints = new TreeMap<String, ContactPoint>();
        loadConfig(filename, element);
    }


    private void loadConfig(String filename, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(filename, element, Arrays.asList(CONFIG_ELEMENT_NAME, CONFIG_ELEMENT_CONTACT_POINTS, CONFIG_ELEMENT_SUBSCRIPTIONS));
        
        for(Element current: elements)
        {
            String elementName = current.getLocalName();
                
            if(elementName.equals(CONFIG_ELEMENT_NAME))
            {
                loadName(filename, current);
            }
            else if(elementName.equals(CONFIG_ELEMENT_CONTACT_POINTS))
            {
                loadContactPoints(filename, current);
            }
            else if(elementName.equals(CONFIG_ELEMENT_SUBSCRIPTIONS))
            {
                loadSubscriptions(filename, current);
            }
            else
            {
                assert(false);
            }
        }        
    }


    private void loadSubscriptions(String filename, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(filename, element, Arrays.asList(CONFIG_ELEMENT_SUBSCRIPTION));
        
        for(Element current: elements)
        {
            String elementName = current.getLocalName();
                
            if(elementName.equals(CONFIG_ELEMENT_SUBSCRIPTION))
            {
                loadSubscription(filename, current);
            }
            else
            {
                assert(false);
            }
        }        
    }
    
    
    private void loadSubscription(String filename, Element element) throws ConfigException
    {
        String projectName = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_SUBSCRIPTION_PROJECT);
        String contactName = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_SUBSCRIPTION_CONTACT_POINT);
        
        if(!theBuilder.hasProject(projectName))
        {
            throw new ConfigException(filename, "Subscription refers to unknown project '" + projectName + "'.");
        }
        
        if(!contactPoints.containsKey(contactName))
        {
            throw new ConfigException(filename, "Subscription refers to unknown contact point '" + contactName + "'.");
        }
        
        theBuilder.getProject(projectName).addSubscription(contactPoints.get(contactName));
    }


    private void loadContactPoints(String filename, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(filename, element, Arrays.asList("email"));
        
        for(Element current: elements)
        {
            String name = XMLConfigUtils.getAttributeValue(filename, current, CONFIG_CONTACT_POINT_NAME);
            
            if(contactPoints.containsKey(name))
            {
                throw new ConfigException(filename, "Duplicate contact point name '" + name + "' specified.");
            }
            
            ContactPoint point = new EmailContactPoint(theBuilder, name, filename, current);
            contactPoints.put(name, point);
        }
    }


    private void loadName(String filename, Element element) throws ConfigException
    {
        name = XMLConfigUtils.getElementText(filename, element);
    }
}
