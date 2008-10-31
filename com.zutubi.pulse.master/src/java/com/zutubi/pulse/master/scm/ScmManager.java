package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;

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
}
