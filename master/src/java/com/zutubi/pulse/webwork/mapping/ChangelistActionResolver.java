package com.zutubi.pulse.webwork.mapping;

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
