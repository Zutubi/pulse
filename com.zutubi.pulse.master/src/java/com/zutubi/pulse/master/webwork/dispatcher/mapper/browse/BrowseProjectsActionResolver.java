package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 */
public class BrowseProjectsActionResolver extends ActionResolverSupport
{
    public BrowseProjectsActionResolver()
    {
        super("browse");
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<project>");
    }

    public ActionResolver getChild(String name)
    {
        return new BrowseProjectActionResolver(name);
    }
}
