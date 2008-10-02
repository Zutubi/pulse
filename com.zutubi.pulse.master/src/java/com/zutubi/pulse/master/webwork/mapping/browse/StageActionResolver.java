package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

/**
 */
public class StageActionResolver extends ActionResolverSupport
{
    public StageActionResolver(String action, String stage)
    {
        super(action);
        addParameter("stageName", stage);
    }

    public ActionResolver getChild(String name)
    {
        return new CommandActionResolver(getAction(), name);
    }
}
