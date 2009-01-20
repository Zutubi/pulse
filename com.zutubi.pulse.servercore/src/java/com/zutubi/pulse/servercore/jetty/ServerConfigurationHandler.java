package com.zutubi.pulse.servercore.jetty;

import org.mortbay.jetty.Server;

import java.io.IOException;

/**
 * The server configuration handler is used to hold the configuration
 * logic for jetty server instances.
 */
public interface ServerConfigurationHandler
{
    /**
     * Configure the server instance.
     *
     * @param server instance to be configured.
     */
    void configure(Server server) throws IOException;
}
