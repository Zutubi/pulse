package com.zutubi.pulse.webwork.mapping;

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
