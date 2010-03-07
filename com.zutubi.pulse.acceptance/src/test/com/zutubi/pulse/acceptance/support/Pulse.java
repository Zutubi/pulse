package com.zutubi.pulse.acceptance.support;

/**
 *
 *
 */
public interface Pulse
{
    int start();
    int start(boolean wait);
    int start(boolean wait, boolean service);

    int stop();
    int stop(long timeout);
    int stop(long timeout, boolean service);
    int waitForProcessToExit(long timeout);

    void setPort(long i);

    void setDataDir(String path);

    void setConfigFile(String path);

    void setContext(String context);

    void setUserHome(String path);

    void setVerbose(boolean verbose);

    boolean ping();

    String getPulseHome();

    String getPluginRoot();

    String getServerUrl();

    String getAdminToken();
}
