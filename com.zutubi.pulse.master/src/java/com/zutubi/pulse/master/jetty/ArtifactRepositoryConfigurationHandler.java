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

    public void configure(ContextHandler context) throws IOException
    {
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
