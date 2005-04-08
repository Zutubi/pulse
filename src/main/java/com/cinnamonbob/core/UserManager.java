package com.cinnamonbob.core;

import nu.xom.Document;
import nu.xom.Element;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manages the users for this Bob.
 */
public class UserManager
{
    private static final String CONFIG_FILENAME     = "users.xml";
    private static final String CONFIG_ELEMENT_USER = "user";
    private static final String CONFIG_USER_LOGIN   = "login";
    
    private Bob               theBuilder;
    private Map<String, User> users;
    
    
    public UserManager(Bob theBuilder) throws ConfigException
    {
        this.theBuilder = theBuilder;
        this.users = new TreeMap<String, User>();
        loadConfig();
    }
    
    private void loadConfig() throws ConfigException
    {
        String filename = theBuilder.getConfigDir().getAbsolutePath() + File.separator + CONFIG_FILENAME;
        
        Document doc = XMLConfigUtils.loadFile(filename);
        loadElements(filename, doc.getRootElement());
    }
    
    
    private void loadElements(String filename, Element root) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(filename, root, Arrays.asList(CONFIG_ELEMENT_USER));
        
        for(Element current: elements)
        {
            String elementName = current.getLocalName();
                
            if(elementName.equals(CONFIG_ELEMENT_USER))
            {
                loadUser(filename, current);
            }
            else
            {
                assert(false);
            }
        }
    }

    private void loadUser(String filename, Element element) throws ConfigException
    {
        String login = XMLConfigUtils.getAttributeValue(filename, element, CONFIG_USER_LOGIN);
                
        if(users.containsKey(login))
        {
            throw new ConfigException(filename, "Duplicate user name '" + login + "' specified.");
        }
        
        User user = new User(theBuilder, login, filename, element);
        users.put(login, user);
    }

}
