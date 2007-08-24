package com.zutubi.pulse.webwork.mapping;

/**
 */
public class BrowseProjectsActionResolver extends ActionResolverSupport
{
    public BrowseProjectsActionResolver()
    {
        super("viewProjects");
    }

    public ActionResolver getChild(String name)
    {
        return new BrowseProjectActionResolver(name);
    }
}
