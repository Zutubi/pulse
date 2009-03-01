package com.zutubi.pulse.servercore.jetty;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpListener;
import org.mortbay.http.handler.NotFoundHandler;
import org.mortbay.jetty.Server;
import org.mortbay.util.InetAddrPort;

import java.io.File;
import java.io.IOException;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

/**
 * The artifact repository provides http access to the file system.
 */
public class ArtifactRepositoryConfigurationHandler implements ServerConfigurationHandler
{
    /**
     * The base directory of the artifact repository on the file system.
     */
    private File base;

    /**
     * The port on which the repository will be available on.
     */
    private int port;

    /**
     * The host address that the repository will respond to.
     */
    private String host;

    private HttpHandler securityHandler;

    public void configure(Server server) throws IOException
    {
        HttpListener listener = CollectionUtils.find(server.getListeners(), new Predicate<HttpListener>()
        {
            public boolean satisfied(HttpListener httpListener)
            {
                return httpListener.getHost().compareTo(host) == 0 && httpListener.getPort() == port;
            }
        });
        if (listener == null)
        {
            server.addListener(new InetAddrPort(host, port));
        }

        HttpContext context = CollectionUtils.find(server.getContexts(), new Predicate<HttpContext>()
        {
            public boolean satisfied(HttpContext httpContext)
            {
                return httpContext.getContextPath().compareTo("/repository") == 0;
            }
        });

        if (context != null)
        {
            throw new IOException("Repository context is already defined.  We can not overide it.");
        }
        context = server.addContext("/repository");

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
        }
        );
        handler.setDirAllowed(true);
        context.addHandler(handler);

        // boilerplate handler for invalid requests.
        context.addHandler(new NotFoundHandler());

        if (server.isStarted())
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

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setSecurityHandler(HttpHandler securityHandler)
    {
        this.securityHandler = securityHandler;
    }
}
