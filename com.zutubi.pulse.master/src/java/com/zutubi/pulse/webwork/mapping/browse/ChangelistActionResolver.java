package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

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
