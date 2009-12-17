package com.zutubi.pulse.core.scm.config;

import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.tove.annotations.Transient;

/**
 * SCM configuration used purely for testing.
 */
public class TestScmConfiguration extends ScmConfiguration
{
    @Transient
    public String getType()
    {
        return "test";
    }

    public String getPreviousRevision(String revision)
    {
        return null;
    }
}
