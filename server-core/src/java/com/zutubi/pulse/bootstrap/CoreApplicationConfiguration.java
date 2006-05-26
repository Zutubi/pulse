package com.zutubi.pulse.bootstrap;

/**
 * Common application configuration shared by master and slaves.
 */
public interface CoreApplicationConfiguration
{
    /**
     * The port on which the http server will listen for connections.
     */
    int getServerPort();
}
