package com.zutubi.pulse.bootstrap;

/**
 * <class-comment/>
 */
public interface StartupManager extends Startup
{
    boolean isSystemStarted();

    long getStartTime();

    void startApplication();

    void continueApplicationStartup();
}
