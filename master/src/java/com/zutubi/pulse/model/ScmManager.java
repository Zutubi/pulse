package com.zutubi.pulse.model;

import java.util.List;

/**
 *
 * 
 */
public interface ScmManager extends EntityManager<Scm>
{
    Scm getScm(long id);

    List<Scm> getActiveScms();

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

    /**
     * Update the default scm polling interval.  This number must be greater
     * than or equal to 1.
     *
     * @param i
     */
    void setDefaultPollingInterval(int i);
}
