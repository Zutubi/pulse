/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

/**
 * <class-comment/>
 */
public interface StartupManager extends Startup
{
    boolean isSystemStarted();

    long getUptime();

    long getStartTime();

    void startApplication();
}
