package com.cinnamonbob.bootstrap.jetty;

import com.cinnamonbob.bootstrap.StartupManager;
import com.cinnamonbob.bootstrap.BootstrapManager;
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
    private BootstrapManager bootstrapManager;
    private WebApplicationContext appContext;

    public void setJettyServer(Server server)
    {
        this.server = server;
    }

    public void setBootstrapManager(BootstrapManager bootstrapManager)
    {
        this.bootstrapManager = bootstrapManager;
    }

    public void deployWebapp() throws Exception
    {
        File wwwRoot = bootstrapManager.getApplicationPaths().getContentRoot();

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
        return (JettyManager) StartupManager.getBean(BEAN_NAME);
    }
}
