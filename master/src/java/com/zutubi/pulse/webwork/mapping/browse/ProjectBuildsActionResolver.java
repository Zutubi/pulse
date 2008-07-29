package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

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
