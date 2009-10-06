package com.zutubi.pulse.core.scm.noop.config;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.tove.annotations.*;

/**
 * Noop SCM configuration.
 */
@Form(fieldOrder = { "checkoutScheme" })
@SymbolicName("zutubi.noopScmConfig")
public class NoopScmConfiguration extends ScmConfiguration
{
    public NoopScmConfiguration()
    {
    }

    public String getType()
    {
        return "noop";
    }
}
