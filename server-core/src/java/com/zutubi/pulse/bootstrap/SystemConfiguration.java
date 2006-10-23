package com.zutubi.pulse.bootstrap;

/**
 * Common application configuration shared by master and slaves.
 */
public interface SystemConfiguration
{
    public static final String WEBAPP_BIND_ADDRESS = "webapp.bindAddress";
    /**
     *
     */
    public static final String WEBAPP_PORT = "webapp.port";

    /**
     *
     */
    public static final String CONTEXT_PATH = "webapp.contextPath";

    /**
     *
     */
    public static final String PULSE_DATA = "pulse.data";

    /**
     * @return the address that we should bind the http server to.
     */
    String getBindAddress();

    /**
     * The port on which the http server will listen for connections.
     *
     * This is a read only property.
     */
    int getServerPort();

    /**
     *
     * This is a read only property.
     */
    String getContextPath();

    void setDataPath(String path);

    String getDataPath();
}
