package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
