package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 */
public class ProjectActionsActionResolver extends ActionResolverSupport
{
    public ProjectActionsActionResolver()
    {
        super(null);
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<project action>");
    }

    public ActionResolver getChild(String name)
    {
        return new ProjectActionActionResolver(name);
    }
}
