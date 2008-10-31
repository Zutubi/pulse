package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

/**
 */
public class ProjectActionsActionResolver extends ActionResolverSupport
{
    public ProjectActionsActionResolver()
    {
        super(null);
    }

    public ActionResolver getChild(String name)
    {
        return new ProjectActionActionResolver(name);
    }
}
