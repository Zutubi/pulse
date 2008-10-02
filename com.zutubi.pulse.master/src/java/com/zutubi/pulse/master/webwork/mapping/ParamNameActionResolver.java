package com.zutubi.pulse.master.webwork.mapping;

/**
 * @see com.zutubi.pulse.master.webwork.mapping.ParameterisedActionResolver
 */
public class ParamNameActionResolver extends ActionResolverSupport
{
    private String name;

    public ParamNameActionResolver(String action, String name)
    {
        super(action);
        this.name = name;
    }

    public ActionResolver getChild(String name)
    {
        return new ParamValueActionResolver(getAction(), this.name, name);
    }
}
