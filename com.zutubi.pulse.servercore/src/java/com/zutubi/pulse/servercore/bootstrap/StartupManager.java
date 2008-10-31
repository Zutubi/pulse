package com.zutubi.pulse.servercore.bootstrap;

/**
 */
public interface StartupManager
{
    void init() throws StartupException;

    boolean isSystemStarted();

    long getStartTime();

    long getUptime();
}
