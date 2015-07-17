package com.zutubi.pulse.servercore.jetty;

import org.eclipse.jetty.server.handler.ContextHandler;

import java.io.IOException;

/**
 * The context configuration handler holds the jetty context configuration
 * logic.
 */
public interface ContextConfigurationHandler
{
    void configure(String contextPath, ContextHandler context) throws IOException;
}
