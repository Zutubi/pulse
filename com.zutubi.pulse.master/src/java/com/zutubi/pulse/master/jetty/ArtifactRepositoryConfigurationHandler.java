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

package com.zutubi.pulse.master.jetty;

import com.zutubi.pulse.servercore.jetty.ContextConfigurationHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;

import java.io.File;
import java.io.IOException;

/**
 * The artifact repository provides http access to the file system.
 */
public class ArtifactRepositoryConfigurationHandler implements ContextConfigurationHandler
{
    /**
     * The base directory of the artifact repository on the file system.
     */
    private File base;

    private Handler securityHandler;

    public void configure(String contextPath, ContextHandler context) throws IOException
    {
        context.setContextPath(contextPath);

        HandlerCollection handlers = new HandlerList();
        handlers.addHandler(securityHandler);

        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(true);
        handlers.addHandler(handler);

        // boilerplate handler for invalid requests.
        handlers.addHandler(new DefaultHandler());

        context.setResourceBase(base.getCanonicalPath());
        context.setHandler(handlers);
    }

    public void setBase(File base)
    {
        this.base = base;
    }

    public void setSecurityHandler(Handler securityHandler)
    {
        this.securityHandler = securityHandler;
    }
}
