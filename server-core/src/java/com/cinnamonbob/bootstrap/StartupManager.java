package com.cinnamonbob.bootstrap;

/**
 * <class-comment/>
 */
public interface StartupManager
{
    void init() throws StartupException;

    boolean isSystemStarted();

    long getUptime();

    long getStartTime();
}
