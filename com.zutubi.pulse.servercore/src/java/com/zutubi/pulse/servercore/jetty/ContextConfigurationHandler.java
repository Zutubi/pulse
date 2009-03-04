package com.zutubi.pulse.servercore.jetty;

import org.mortbay.http.HttpContext;

import java.io.IOException;

/**
 * The context configuration handler is holds the jetty context configuration
 * logic.
 */
public interface ContextConfigurationHandler
{
    void configure(HttpContext context) throws IOException;
}
