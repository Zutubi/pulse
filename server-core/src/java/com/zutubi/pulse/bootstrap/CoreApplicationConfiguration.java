package com.zutubi.pulse.bootstrap;

/**
 * Common application configuration shared by master and slaves.
 */
public interface CoreApplicationConfiguration
{
    public static final String WEBAPP_PORT = "webapp.port";
    public static final String LOGGING_CONFIG = "log.config";

    /**
     * The port on which the http server will listen for connections.
     */
    int getServerPort();

    String getLogConfig();
}
