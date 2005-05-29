package com.cinnamonbob.core;

import com.cinnamonbob.core.scm.P4Server;
import com.cinnamonbob.core.scm.SCMException;
import com.cinnamonbob.core.scm.SCMServer;
import nu.xom.Element;

import java.io.File;

/**
 * A command for checking out code from a Subversion repository.
 * 
 * @author jsankey
 */
public class P4CheckoutCommand extends SCMCheckoutCommand
{
    private static final String CONFIG_ATTR_PORT       = "port";
    private static final String CONFIG_ATTR_USER       = "user";
    private static final String CONFIG_ATTR_PASSWORD   = "password";
    private static final String CONFIG_ATTR_CLIENT     = "client";
    private static final String CONFIG_ATTR_PATH       = "path";
    private static final String VARIABLE_PORT          = "p4.port";
    private static final String VARIABLE_USER          = "p4.user";
    private static final String VARIABLE_PASSWORD      = "p4.password";
    private static final String VARIABLE_CLIENT        = "p4.client";

    private String port;
    private String user;
    private String password;
    private String client;
    private File   path;
    
    
    private void loadConfig(ConfigContext context, Element element) throws ConfigException
    {
        port     = XMLConfigUtils.getOptionalAttributeValue(context, element, CONFIG_ATTR_PORT, context.getVariableValue(VARIABLE_PORT));
        user     = XMLConfigUtils.getOptionalAttributeValue(context, element, CONFIG_ATTR_USER, context.getVariableValue(VARIABLE_USER));
        password = XMLConfigUtils.getOptionalAttributeValue(context, element, CONFIG_ATTR_PASSWORD, context.getVariableValue(VARIABLE_PASSWORD));
        client   = XMLConfigUtils.getOptionalAttributeValue(context, element, CONFIG_ATTR_CLIENT, context.getVariableValue(VARIABLE_CLIENT));
        path     = new File(XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_PATH));
    }
    
    public P4CheckoutCommand(ConfigContext context, Element element, CommandCommon common) throws ConfigException
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
        return new P4Server(port, user, password, client);
    }
    
    @Override
    protected void destroyServer(SCMServer server)
    {
        // Nothing to do
    }
    
    public void setPort(String port)
    {
        this.port = port;
    }
    
    public void setUser(String user)
    {
        this.user = user;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    public void setClient(String client)
    {
        this.client = client;
    }
    public void setPath(File path)
    {
        this.path = path;
    }
}
