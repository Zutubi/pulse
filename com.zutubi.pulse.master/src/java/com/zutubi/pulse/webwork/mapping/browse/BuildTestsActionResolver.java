package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

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
