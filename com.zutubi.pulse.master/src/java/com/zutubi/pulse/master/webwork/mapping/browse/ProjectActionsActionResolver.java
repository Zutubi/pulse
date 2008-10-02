package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
