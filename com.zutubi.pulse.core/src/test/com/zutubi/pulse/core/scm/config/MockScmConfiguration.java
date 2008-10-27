package com.zutubi.pulse.core.scm.config;

import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;

/**
 *
 *
 */
public class MockScmConfiguration extends ScmConfiguration
{
    @Transient
    public String getType()
    {
        return "mock";
    }

    public String getPreviousRevision(String revision)
    {
        return null;
    }
}
