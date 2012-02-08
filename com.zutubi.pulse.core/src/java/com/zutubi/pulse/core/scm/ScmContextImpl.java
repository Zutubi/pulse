package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.api.PersistentContext;
import com.zutubi.pulse.core.scm.api.ScmContext;

/**
 * A simple implementation of {@link com.zutubi.pulse.core.scm.api.ScmContext}.
 */
public class ScmContextImpl implements ScmContext
{
    private PersistentContext persistentContext;
    private ExecutionContext environmentContext;

    public ScmContextImpl(PersistentContext persistentContext, ExecutionContext environmentContext)
    {
        this.persistentContext = persistentContext;
        this.environmentContext = environmentContext;
    }

    public PersistentContext getPersistentContext()
    {
        return persistentContext;
    }

    public ExecutionContext getEnvironmentContext()
    {
        return environmentContext;
    }
}
