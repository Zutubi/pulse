package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
