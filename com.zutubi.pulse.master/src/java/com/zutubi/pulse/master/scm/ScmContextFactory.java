package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.core.scm.ScmException;

/**
 * Interface for the scm context factory.  The context factory is used to maintain the
 * scm context instances.  These instances hold persistent data between scm invocations.
 */
public interface ScmContextFactory
{
    ScmContext createContext(long projectId, ScmConfiguration scm) throws ScmException;
}
