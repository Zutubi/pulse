package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
