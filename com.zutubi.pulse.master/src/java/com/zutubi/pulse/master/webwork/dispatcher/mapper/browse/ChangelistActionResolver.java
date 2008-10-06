package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

/**
 */
public class ChangelistActionResolver extends ActionResolverSupport
{
    public ChangelistActionResolver(String id)
    {
        super("viewChangelist");
        addParameter("id", id);
    }
}
