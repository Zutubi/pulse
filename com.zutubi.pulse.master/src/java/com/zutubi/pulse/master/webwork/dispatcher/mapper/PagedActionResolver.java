package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.Arrays;
import java.util.List;

/**
 * A resolver that serves as a base for actions that support paging.
 */
public class PagedActionResolver extends ActionResolverSupport
{
    public PagedActionResolver(String action)
    {
        super(action);
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<page number>");
    }

    public ActionResolver getChild(String name)
    {
        return new PageActionResolver(getAction(), name);
    }
}
