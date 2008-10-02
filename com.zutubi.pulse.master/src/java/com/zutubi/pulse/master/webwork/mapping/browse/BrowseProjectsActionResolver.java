package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
