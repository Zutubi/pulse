package com.zutubi.pulse.model;

import java.util.List;

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
