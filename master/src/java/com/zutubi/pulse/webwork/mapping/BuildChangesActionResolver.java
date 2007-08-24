package com.zutubi.pulse.webwork.mapping;

/**
 */
public class BuildChangesActionResolver extends ActionResolverSupport
{
    public BuildChangesActionResolver()
    {
        super("viewChanges");
    }

    public ActionResolver getChild(String name)
    {
        if(name.equals("sinceBuild"))
        {
            return new ParamNameActionResolver(getAction(), name);
        }
        else
        {
            return new ChangelistActionResolver(name);
        }
    }
}
