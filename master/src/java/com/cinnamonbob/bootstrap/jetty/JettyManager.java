package com.cinnamonbob.jetty;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

import java.io.File;

/**
 * 
 *
 */
public class JettyManager
{
    private static final String BEAN_NAME = "jettyManager";

    private Server server;
    private ConfigurationManager configurationManager;
    private WebApplicationContext appContext;

    public void setJettyServer(Server server)
    {
        this.server = server;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void deployWebapp() throws Exception
    {
        File wwwRoot = configurationManager.getApplicationPaths().getContentRoot();

        appContext = server.addWebApplication("/", wwwRoot.getAbsolutePath());

        if (!server.isStarted())
        {
            server.start();
        }
    }

    public void deployInWebApplicationContext(String name, Object obj)
    {
        appContext.setAttribute(name, obj);
    }

    public static JettyManager getInstance()
    {
        return (JettyManager) ComponentContext.getBean(BEAN_NAME);
    }
}
