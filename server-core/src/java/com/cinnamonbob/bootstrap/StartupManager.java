package com.cinnamonbob.bootstrap;

/**
 * <class-comment/>
 */
public interface StartupManager
{
    /**
     * Trigger a system startup.
     */
    void startup() throws StartupException;

    /**
     * Get the amount of time since the system started.
     *
     * @return time in milliseconds.
     */
    long getUptime();

    /**
     * Get the time that the system started.
     *
     * @return time in milliseconds.
     */
    long getStartTime();

    /**
     * Indicates whether or not the system has been started. The system is started
     * by calling the startup method in this interface.
     *
     * @return true if the system has started, false otherwise
     */
    boolean isSystemStarted();
}
