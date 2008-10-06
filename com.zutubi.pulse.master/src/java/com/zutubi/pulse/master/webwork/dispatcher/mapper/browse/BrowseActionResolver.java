package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

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
