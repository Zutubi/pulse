package com.zutubi.pulse.webwork.mapping;

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
