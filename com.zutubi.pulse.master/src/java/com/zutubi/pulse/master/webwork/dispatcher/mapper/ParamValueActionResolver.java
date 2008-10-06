package com.zutubi.pulse.master.webwork.dispatcher.mapper;

/**
 * @see com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver
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
