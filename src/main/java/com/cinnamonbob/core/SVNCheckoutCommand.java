package com.cinnamonbob.core;

import java.io.File;

import com.cinnamonbob.core.scm.SCMException;
import com.cinnamonbob.core.scm.SCMServer;
import com.cinnamonbob.core.scm.SVNServer;

import nu.xom.Element;

/**
 * A command for checking out code from a Subversion repository.
 * 
 * @author jsankey
 */
public class SVNCheckoutCommand extends SCMCheckoutCommand
{
    private static final String CONFIG_ATTR_USER       = "user";
    private static final String CONFIG_ATTR_PASSWORD   = "password";
    private static final String CONFIG_ATTR_KEY_FILE   = "key-file";
    private static final String CONFIG_ATTR_PASSPHRASE = "passphrase";
    private static final String CONFIG_ATTR_URL        = "url";
    private static final String CONFIG_ATTR_PATH       = "path";
    private static final String VARIABLE_USER          = "svn.user";
    private static final String VARIABLE_PASSWORD      = "svn.password";
    private static final String VARIABLE_KEY_FILE      = "svn.keyfile";
    private static final String VARIABLE_PASSPHRASE    = "svn.passphrase";
    private static final String VARIABLE_URL           = "svn.url";

    private String user;
    private String password;
    private String keyFile;
    private String passphrase;
    private String url;
    private File   path;
    
    
    private void loadConfig(ConfigContext context, Element element) throws ConfigException
    {
        user       = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_USER, context.getVariableValue(VARIABLE_USER));
        password   = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_PASSWORD, context.getVariableValue(VARIABLE_PASSWORD));
        keyFile    = XMLConfigUtils.getOptionalAttributeValue(context, element, CONFIG_ATTR_KEY_FILE, context.getVariableValue(VARIABLE_KEY_FILE));
        passphrase = XMLConfigUtils.getOptionalAttributeValue(context, element, CONFIG_ATTR_PASSPHRASE, context.getVariableValue(VARIABLE_PASSPHRASE));
        url        = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_URL, context.getVariableValue(VARIABLE_URL));
        path       = new File(XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_PATH));
    }
    
    public SVNCheckoutCommand(ConfigContext context, Element element, CommandCommon common) throws ConfigException
    {
        super(common);
        loadConfig(context, element);        
    }

    @Override
    protected File getPath()
    {
        return path;
    }

    @Override
    protected SCMServer createServer() throws SCMException
    {
        if(keyFile == null)
        {
            return new SVNServer(url, user, password);
        }
        else
        {
            if(passphrase == null)
            {
                return new SVNServer(url, user, password, keyFile);
            }
            else
            {
                return new SVNServer(url, user, password, keyFile, passphrase);                
            }
        }
    }
    
    @Override
    protected void destroyServer(SCMServer server)
    {
        // Nothing to do
    }
}
