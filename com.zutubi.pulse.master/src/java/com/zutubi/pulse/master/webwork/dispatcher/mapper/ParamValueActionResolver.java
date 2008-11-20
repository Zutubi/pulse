package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.Arrays;
import java.util.List;

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

    public List<String> listChildren()
    {
        return Arrays.asList("<name>");
    }

    public ActionResolver getChild(String name)
    {
        return new ParamNameActionResolver(getAction(), name);
    }
}
