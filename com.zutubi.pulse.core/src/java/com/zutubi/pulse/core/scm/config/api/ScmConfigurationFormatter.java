package com.zutubi.pulse.core.scm.config.api;

public class ScmConfigurationFormatter
{
    public String getType(ScmConfiguration scm)
    {
        return scm.getType();
    }

    public String getSummary(ScmConfiguration scm)
    {
        return scm.getSummary();
    }
}
