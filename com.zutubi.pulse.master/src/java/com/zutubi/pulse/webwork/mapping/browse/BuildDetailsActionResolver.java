package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.ActionResolverSupport;

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
