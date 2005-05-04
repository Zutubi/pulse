package com.cinnamonbob.core;

import com.cinnamonbob.bootstrap.ApplicationPaths;
import com.cinnamonbob.bootstrap.BootstrapUtils;
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
        ApplicationPaths paths = BootstrapUtils.getManager().getApplicationPaths();

        String filename = paths.getUserConfigRoot().getAbsolutePath() + File.separator + CONFIG_FILENAME;
        
        Document      doc     = XMLConfigUtils.loadFile(filename);
        ConfigContext context = new ConfigContext(filename);
        loadElements(context, doc.getRootElement());
    }
    
    
    private void loadElements(ConfigContext context, Element root) throws ConfigException
    {
        List<Element> elements = XMLConfigUtils.getElements(context, root, Arrays.asList(CONFIG_ELEMENT_USER));
        
        for(Element current: elements)
        {
            String elementName = current.getLocalName();
                
            if(elementName.equals(CONFIG_ELEMENT_USER))
            {
                loadUser(context, current);
            }
            else
            {
                assert(false);
            }
        }
    }

    private void loadUser(ConfigContext context, Element element) throws ConfigException
    {
        String login = XMLConfigUtils.getAttributeValue(context, element, CONFIG_USER_LOGIN);
                
        if(users.containsKey(login))
        {
            throw new ConfigException(context.getFilename(), "Duplicate user name '" + login + "' specified.");
        }
        
        User user = new User(theBuilder, login, context, element);
        users.put(login, user);
    }

}
