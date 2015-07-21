package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * A task that dynamically deploys the RESTish API servlet (serving /api/*). This happens after the
 * configuration subsystem is available so those services can be wired into controllers.
 */
public class DeployApiServletStartupTask implements StartupTask
{
    private JettyServerManager jettyServerManager;

    @Override
    public void execute() throws Exception
    {
        XmlWebApplicationContext xmlWebApplicationContext = new XmlWebApplicationContext();
        xmlWebApplicationContext.setParent(SpringComponentContext.getContext());
        ServletHolder servletHolder = new ServletHolder("api", new DispatcherServlet(xmlWebApplicationContext));
        WebAppContext webAppContext = jettyServerManager.getContextHandler(WebAppContext.class);
        webAppContext.addServlet(servletHolder, "/api/*");
    }

    @Override
    public boolean haltOnFailure()
    {
        return true;
    }

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }
}
