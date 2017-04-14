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

package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.spring.web.context.SpringSecurityFilter;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * The Pulse Security Manager is responsible for enabling and
 * disabling web based security.
 */
public class PulseSecurityManager implements SecurityManager
{
    private static final String SECURITY_FILTER_NAME = "security";

    private static final Logger LOG = Logger.getLogger(PulseSecurityManager.class);

    private JettyServerManager jettyServerManager;

    /**
     * Enable security in the Pulse web application.
     */
    public void secure()
    {
        WebAppContext handler = getHandler();
        if (handler == null)
        {
            throw new RuntimeException("Can not enable web security before the web app is fully deployed.");
        }
        enableWebUISecurity(handler);
    }

    private void enableWebUISecurity(WebAppContext handler)
    {
        FilterHolder holder = handler.getServletHandler().getFilter(SECURITY_FILTER_NAME);
        try
        {
            SpringSecurityFilter filter = (SpringSecurityFilter) holder.getFilter();
            filter.enableSecurity();
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    private WebAppContext getHandler()
    {
        Server server = jettyServerManager.getServer();
        if (server.isStarted())
        {
            return jettyServerManager.getContextHandler(WebAppContext.class);
        }
        return null;
    }

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }
}
