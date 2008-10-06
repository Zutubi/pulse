package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

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
