package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.PagedActionResolver;

/**
 */
public class ChangelistActionResolver extends PagedActionResolver
{
    public ChangelistActionResolver(String id)
    {
        super("viewChangelist");
        addParameter("id", id);
    }
}
