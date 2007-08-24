package com.zutubi.pulse.webwork.mapping;

/**
 */
public class BrowseActionResolver extends StaticMapActionResolver
{
    public BrowseActionResolver()
    {
        super("viewProjects");
        addMapping("projects", new BrowseProjectsActionResolver());
    }
}
