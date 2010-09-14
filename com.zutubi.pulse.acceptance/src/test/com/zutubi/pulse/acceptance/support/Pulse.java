package com.zutubi.pulse.acceptance.support;

/**
 * A handle to an installed Pulse server.
 */
public interface Pulse
{
    /**
     * Start this Pulse instance.  By default, we wait for Pulse to start before returning.  If Pulse
     * has not started within 300 seconds, then this method throws an exception.
     * 
     * @return the startup commands exit status.
     * @throws Exception if there is a problem starting Pulse, or if the startup times out.
     */
    int start() throws Exception;
    int start(boolean wait);
    int start(boolean wait, boolean service);

    int stop();
    int stop(long timeout);
    int stop(long timeout, boolean service);
    int waitForProcessToExit(long timeout);

    /**
     * Set the port on which the Pulse instance will start.
     *
     * @param port  to which the pulse instance will bind.
     */
    void setPort(long port);

    void setDataDir(String path);

    void setConfigFile(String path);

    void setContext(String context);

    void setUserHome(String path);

    void setVerbose(boolean verbose);

    /**
     * Ping the pulse instance, returning true if we receive a response, false otherwise.
     *
     * @return true if the instance can be reached.
     */
    boolean ping();

    String getScript();
    
    String getPulseHome();

    String getActiveVersionDirectory();
    
    String getPluginRoot();

    String getServerUrl();

    String getAdminToken();
}
