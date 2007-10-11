package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

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
