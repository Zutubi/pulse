package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
