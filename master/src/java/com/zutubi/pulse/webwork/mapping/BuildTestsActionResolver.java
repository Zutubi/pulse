package com.zutubi.pulse.webwork.mapping;

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
