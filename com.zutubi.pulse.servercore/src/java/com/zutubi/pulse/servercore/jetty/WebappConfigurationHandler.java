package com.zutubi.pulse.servercore.jetty;

import org.mortbay.jetty.servlet.WebApplicationContext;

import java.io.IOException;

/**
 * The context configuration handler is holds the jetty web context configuration
 * logic.
 */
public interface WebappConfigurationHandler
{
    void configure(WebApplicationContext context) throws IOException;
}
