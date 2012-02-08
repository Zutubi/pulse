package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;

/**
 * A service for creating {@link ScmContext} instances.  Note that SCM plugin
 * authors will rarely need to create contexts, they are provided by Pulse.  A
 * common exception is configuration check handlers, which may need to generate
 * a context via this factory.
 * <p/>
 * To get an instance of this factory, annotate your configuration check class
 * with {@link com.zutubi.tove.annotations.Wire} and declare a setter named
 * setScmContextFactory.
 */
public interface ScmContextFactory
{
    /**
     * Creates a context for the given configuration.
     *
     * @param scmConfiguration SCM configuration to create a context for
     * @param implicitResource the name of a resource to implicitly import into
     *        the context (may be null), see {@link com.zutubi.pulse.core.scm.api.ScmClient#getImplicitResource()}
     * @return the created context
     */
    ScmContext createContext(ScmConfiguration scmConfiguration, String implicitResource);
}
