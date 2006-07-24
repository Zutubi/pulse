package com.zutubi.pulse.bootstrap;

/**
 * Common application configuration shared by master and slaves.
 */
public interface SystemConfiguration
{
    public static final String WEBAPP_PORT = "webapp.port";


    public static final String CONTEXT_PATH = "webapp.contextPath";

    /**
     * The port on which the http server will listen for connections.
     */
    int getServerPort();

    String getContextPath();

}
