package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

/**
 */
public class ChangelistsActionResolver extends ActionResolverSupport
{
    public ChangelistsActionResolver()
    {
        super(null);
    }

    public ActionResolver getChild(String name)
    {
        return new ChangelistActionResolver(name);
    }
}
