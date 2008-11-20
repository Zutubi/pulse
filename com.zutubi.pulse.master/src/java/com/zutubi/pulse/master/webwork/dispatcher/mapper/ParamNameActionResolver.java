package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.Arrays;
import java.util.List;

/**
 * @see com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver
 */
public class ParamNameActionResolver extends ActionResolverSupport
{
    private String name;

    public ParamNameActionResolver(String action, String name)
    {
        super(action);
        this.name = name;
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<value>");
    }

    public ActionResolver getChild(String name)
    {
        return new ParamValueActionResolver(getAction(), this.name, name);
    }
}
