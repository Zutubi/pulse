package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;

/**
 * The scm manager handles the basic management of the background scm processes
 * and life cycle.
 */
public interface ScmManager extends MasterScmContextFactory, ScmClientFactory<ScmConfiguration>
{
    /**
     * Clears any cached information for the project with the given id.  This
     * should be called whenever the SCM details for a project change
     * significantly.  Note that a context lock for the given project must be
     * held when calling this method.
     *
     * @param projectId the project to clear the cache for
     */
    void clearCache(long projectId);
}
