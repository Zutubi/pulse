package com.zutubi.pulse.webwork.mapping;

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
