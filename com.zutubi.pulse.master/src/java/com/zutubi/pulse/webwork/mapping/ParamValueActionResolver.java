package com.zutubi.pulse.webwork.mapping;

/**
 * @see com.zutubi.pulse.webwork.mapping.ParameterisedActionResolver
 */
public class ParamValueActionResolver extends ActionResolverSupport
{
    public ParamValueActionResolver(String action, String name, String value)
    {
        super(action);
        addParameter(name, value);
    }

    public ActionResolver getChild(String name)
    {
        return new ParamNameActionResolver(getAction(), name);
    }
}
