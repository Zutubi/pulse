package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.StaticMapActionResolver;

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
