package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

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
