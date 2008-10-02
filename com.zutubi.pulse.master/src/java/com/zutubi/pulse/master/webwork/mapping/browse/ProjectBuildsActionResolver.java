package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

/**
 */
public class ProjectBuildsActionResolver extends ActionResolverSupport
{
    public ProjectBuildsActionResolver()
    {
        super(null);
    }

    public ActionResolver getChild(String name)
    {
        return new BuildActionResolver(name);
    }
}
