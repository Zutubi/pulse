package com.zutubi.pulse.webwork.mapping;

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
