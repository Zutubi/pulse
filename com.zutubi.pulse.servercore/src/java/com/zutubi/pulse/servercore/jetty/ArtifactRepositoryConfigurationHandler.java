package com.zutubi.pulse.servercore.jetty;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.handler.NotFoundHandler;

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

    private HttpHandler securityHandler;

    public void configure(HttpContext context) throws IOException
    {
        context.setResourceBase(base.getCanonicalPath());
        context.addHandler(securityHandler);

        // the resource handler does all of the file system work.
        context.addHandler(new CreateRepositoryDirectoryHandler());
        ResourceHandler handler = new ResourceHandler();
        handler.setAllowedMethods(new String[]{HttpRequest.__GET,
                HttpRequest.__HEAD,
                HttpRequest.__DELETE,
                HttpRequest.__OPTIONS,
                HttpRequest.__PUT
        });
        handler.setDirAllowed(true);
        context.addHandler(handler);

        // boilerplate handler for invalid requests.
        context.addHandler(new NotFoundHandler());

        // start the context if the server into which we are adding it has already been started.
        if (context.getHttpServer().isStarted())
        {
            try
            {
                context.start();
            }
            catch (Exception e)
            {
                throw new IOException("Failed to start repository context.");
            }
        }
    }

    public void setBase(File base)
    {
        this.base = base;
    }

    public void setSecurityHandler(HttpHandler securityHandler)
    {
        this.securityHandler = securityHandler;
    }
}
