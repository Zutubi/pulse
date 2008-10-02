package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.StaticMapActionResolver;

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
