package com.zutubi.pulse.master.webwork.dispatcher.mapper.dashboard;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.browse.BuildActionResolver;

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
