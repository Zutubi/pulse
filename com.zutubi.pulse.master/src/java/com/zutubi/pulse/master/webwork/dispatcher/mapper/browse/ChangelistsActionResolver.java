package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 */
public class ChangelistsActionResolver extends ActionResolverSupport
{
    public ChangelistsActionResolver()
    {
        super(null);
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<changelist id>");
    }

    public ActionResolver getChild(String name)
    {
        return new ChangelistActionResolver(name);
    }

}
