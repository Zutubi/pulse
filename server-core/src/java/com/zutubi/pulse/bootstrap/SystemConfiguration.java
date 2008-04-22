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

    public static final String RESTORE_FILE = "pulse.restore.file";

    public static final String SSL_ENABLED      = "ssl.enabled";
    public static final String SSL_KEYSTORE     = "ssl.keystore";
    public static final String SSL_PASSWORD     = "ssl.password";
    public static final String SSL_KEY_PASSWORD = "ssl.keyPassword";

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

    String getContextPathNormalised();

    String getRestoreFile();

    void setDataPath(String path);

    String getDataPath();

    boolean isSslEnabled();
    String getSslKeystore();
    String getSslPassword();
    String getSslKeyPassword();

}
