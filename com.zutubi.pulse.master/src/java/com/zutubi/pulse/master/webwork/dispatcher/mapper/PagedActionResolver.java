package com.zutubi.pulse.master.webwork.dispatcher.mapper;

/**
 * A resolver that serves as a base for actions that support paging.
 */
public class PagedActionResolver extends ActionResolverSupport
{
    public PagedActionResolver(String action)
    {
        super(action);
    }

    public ActionResolver getChild(String name)
    {
        return new PageActionResolver(getAction(), name);
    }
}
