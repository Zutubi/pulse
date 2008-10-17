package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;

/**
 * The scm manager handles the basic management of the background scm processes
 * and life cycle.  In particular, it is responsible for the initialisation
 * of the scms and the regular polling of scms for changes.
 */
public interface ScmManager extends ScmContextFactory, ScmClientFactory<ScmConfiguration>
{
    /**
     * Trigger the polling of all active scms.  An active scm is one that is considered
     * ready, implements the pollable interface, and has monitoring enabled.
     */
    void pollActiveScms();

    /**
     * Returns true if the scm is ready to be used.  That is, if its initialisation
     * has successfully completed.  An scm needs to be 'ready' before is ScmContext
     * is valid and usable.
     *
     * @param scm configuration of the scm being queried.
     *
     * @return true if ready, false otherwise.
     */
    boolean isReady(ScmConfiguration scm);
}
