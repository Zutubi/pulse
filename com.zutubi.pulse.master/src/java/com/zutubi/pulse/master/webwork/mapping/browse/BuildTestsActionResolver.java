package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
