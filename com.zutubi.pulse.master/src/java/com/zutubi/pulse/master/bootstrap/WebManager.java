/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.bootstrap;

import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.security.SecurityManager;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * The web manager is responsible for handling processes related
 * to the management and configuration of the web application(s)
 * deployed within the embedded jetty instances.
 */
public class WebManager
{
    public static final String REPOSITORY_PATH = "/repository";

    private boolean mainDeployed = false;

    private JettyServerManager jettyServerManager;

    /**
     * Load the setup process' xwork configuration.
     */
    public void deploySetup()
    {
        loadXworkConfiguration("xwork-setup.xml");

        deployApi("setup-api");
    }

    /**
     * Deploy the main pulse applications' xwork configuration.
     */
    public void deployMain()
    {
        mainDeployed = true;
        loadXworkConfiguration("xwork.xml");

        // enable security only when the standard xwork file is loaded.
        SecurityManager securityManager = SpringComponentContext.getBean("securityManager");
        securityManager.secure();

        deployApi("api");
    }

    public void deployApi(String servletName)
    {
        XmlWebApplicationContext xmlWebApplicationContext = new XmlWebApplicationContext();
        xmlWebApplicationContext.setParent(SpringComponentContext.getContext());

        DispatcherServlet servlet = new DispatcherServlet(xmlWebApplicationContext);
        // This allows the ApiExceptionHandler to process no-handler-found just like other
        // exceptions.
        servlet.setThrowExceptionIfNoHandlerFound(true);

        ServletHolder servletHolder = new ServletHolder(servletName, servlet);
        WebAppContext webAppContext = jettyServerManager.getContextHandler(WebAppContext.class);
        webAppContext.addServlet(servletHolder,  "/" + servletName + "/*");
    }

    public boolean isMainDeployed()
    {
        return mainDeployed;
    }

    private void loadXworkConfiguration(String name)
    {
        com.opensymphony.xwork.config.ConfigurationManager.clearConfigurationProviders();
        com.opensymphony.xwork.config.ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider(name));
        com.opensymphony.xwork.config.ConfigurationManager.getConfiguration().reload();
    }

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }
}
