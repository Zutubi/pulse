package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 */
public class BuildDetailsActionResolver extends ActionResolverSupport
{
    public BuildDetailsActionResolver()
    {
        super("viewDetails");
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<stage>");
    }

    public ActionResolver getChild(String name)
    {
        return new StageActionResolver(getAction(), name);
    }
}
