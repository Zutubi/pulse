package com.zutubi.pulse.servercore.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.util.InetAddrPort;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.http.handler.NotFoundHandler;

import javax.servlet.Filter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

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
        server.addListener(new InetAddrPort(host, port));
        HttpContext context = server.addContext("/");
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
