package com.zutubi.pulse.core.scm.config;

import com.zutubi.config.annotations.Transient;

/**
 *
 *
 */
public class MockScmConfiguration extends com.zutubi.pulse.core.scm.config.ScmConfiguration
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
