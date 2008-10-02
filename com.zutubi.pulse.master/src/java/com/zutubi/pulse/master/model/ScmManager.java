package com.zutubi.pulse.master.model;

/**
 *
 * 
 */
public interface ScmManager
{
    void pollActiveScms();

    /**
     * Retrieve the default scm polling interval.
     *
     * The scm polling interval defines the amount of time to wait between
     * successive requests to an scm asking it whether it has changed.
     *
     * @return
     */
    int getDefaultPollingInterval();
}
