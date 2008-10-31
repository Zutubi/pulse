package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

/**
 */
public class BuildDetailsActionResolver extends ActionResolverSupport
{
    public BuildDetailsActionResolver()
    {
        super("viewCommandLog");
    }

    public ActionResolver getChild(String name)
    {
        return new StageActionResolver(getAction(), name);
    }
}
