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
    private static final String CONFIG_SUBSCRIPTION_FAILED        = "failed-only";
    
    
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

    
    public User(Bob theBuilder, String login, ConfigContext context, Element element) throws ConfigException
    {
        this.theBuilder = theBuilder;
        this.login = login;
        this.contactPoints = new TreeMap<String, ContactPoint>();
        loadConfig(context, element);
    }


    private void loadConfig(ConfigContext context, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList(CONFIG_ELEMENT_NAME, CONFIG_ELEMENT_CONTACT_POINTS, CONFIG_ELEMENT_SUBSCRIPTIONS));
        
        for(Element current: elements)
        {
            String elementName = current.getLocalName();
                
            if(elementName.equals(CONFIG_ELEMENT_NAME))
            {
                loadName(context, current);
            }
            else if(elementName.equals(CONFIG_ELEMENT_CONTACT_POINTS))
            {
                loadContactPoints(context, current);
            }
            else if(elementName.equals(CONFIG_ELEMENT_SUBSCRIPTIONS))
            {
                loadSubscriptions(context, current);
            }
            else
            {
                assert(false);
            }
        }        
    }


    private void loadSubscriptions(ConfigContext context, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList(CONFIG_ELEMENT_SUBSCRIPTION));
        
        for(Element current: elements)
        {
            String elementName = current.getLocalName();
                
            if(elementName.equals(CONFIG_ELEMENT_SUBSCRIPTION))
            {
                loadSubscription(context, current);
            }
            else
            {
                assert(false);
            }
        }        
    }
    
    
    private void loadSubscription(ConfigContext context, Element element) throws ConfigException
    {
        String projectName = XMLConfigUtils.getAttributeValue(context, element, CONFIG_SUBSCRIPTION_PROJECT);
        String contactName = XMLConfigUtils.getAttributeValue(context, element, CONFIG_SUBSCRIPTION_CONTACT_POINT);
        String failed      = element.getAttributeValue(CONFIG_SUBSCRIPTION_FAILED);
        
        if(!theBuilder.hasProject(projectName))
        {
            throw new ConfigException(context.getFilename(), "Subscription refers to unknown project '" + projectName + "'.");
        }
        
        if(!contactPoints.containsKey(contactName))
        {
            throw new ConfigException(context.getFilename(), "Subscription refers to unknown contact point '" + contactName + "'.");
        }
        
        Project.Event event = Project.Event.BUILD_COMPLETE;
        
        if(failed != null)
        {
            event = Project.Event.BUILD_FAILED;
        }
        
        theBuilder.getProject(projectName).addSubscription(event, contactPoints.get(contactName));
    }


    private void loadContactPoints(ConfigContext context, Element element) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, element, Arrays.asList("email"));
        
        for(Element current: elements)
        {
            String name = XMLConfigUtils.getAttributeValue(context, current, CONFIG_CONTACT_POINT_NAME);
            
            if(contactPoints.containsKey(name))
            {
                throw new ConfigException(context.getFilename(), "Duplicate contact point name '" + name + "' specified.");
            }
            
            ContactPoint point = new EmailContactPoint(theBuilder, name, context, current);
            contactPoints.put(name, point);
        }
    }


    private void loadName(ConfigContext context, Element element) throws ConfigException
    {
        name = XMLConfigUtils.getElementText(context, element);
    }

    /**
     * The users login.
     * @return
     */
    public String getLogin()
    {
        return login;
    }

    void setLogin(String login)
    {
        this.login = login;
    }

    /**
     * The users name.
     * @return
     */
    public String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name = name;
    }

}
