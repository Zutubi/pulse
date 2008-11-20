package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 */
public class ProjectBuildsActionResolver extends ActionResolverSupport
{
    public ProjectBuildsActionResolver()
    {
        super(null);
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<build>");
    }

    public ActionResolver getChild(String name)
    {
        return new BuildActionResolver(name);
    }
}
