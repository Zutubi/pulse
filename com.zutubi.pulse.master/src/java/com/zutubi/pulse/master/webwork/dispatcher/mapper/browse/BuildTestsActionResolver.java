package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

/**
 */
public class BuildTestsActionResolver extends ActionResolverSupport
{
    public BuildTestsActionResolver()
    {
        super("viewTests");
    }

    public ActionResolver getChild(String name)
    {
        return new StageTestsActionResolver(name);
    }
}
