package com.zutubi.pulse.bootstrap;

/**
 * <class-comment/>
 */
public interface StartupManager
{
    void init() throws StartupException;

    boolean isSystemStarted();

    long getUptime();

    long getStartTime();

    void startApplication();

    void continueApplicationStartup();
}
