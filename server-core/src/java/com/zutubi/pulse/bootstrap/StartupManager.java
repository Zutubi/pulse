package com.zutubi.pulse.bootstrap;

/**
 */
public interface StartupManager
{
    void init() throws StartupException;

    boolean isSystemStarted();

    long getStartTime();

    long getUptime();
}
