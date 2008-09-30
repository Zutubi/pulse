package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

/**
 */
public class BuildLogsActionResolver extends ActionResolverSupport
{
    public BuildLogsActionResolver()
    {
        super(null);
    }

    public ActionResolver getChild(String name)
    {
        return new StageLogsActionResolver(name);
    }
}
