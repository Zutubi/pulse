package com.zutubi.pulse.webwork.mapping.dashboard;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;
import com.zutubi.pulse.webwork.mapping.browse.BuildActionResolver;

/**
 */
public class MyBuildsActionResolver extends ActionResolverSupport
{
    public MyBuildsActionResolver()
    {
        super("my");
        addParameter("personal", "true");
    }

    public ActionResolver getChild(String name)
    {
        return new BuildActionResolver(name);
    }
}
