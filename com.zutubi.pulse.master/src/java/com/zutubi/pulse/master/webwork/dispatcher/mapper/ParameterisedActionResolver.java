package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.Arrays;
import java.util.List;

/**
 * A resolver that allows arbitrary parameters to be appended to the url in
 * the form <name>/<value>/.  It accepts children as the parameter name,
 * returning a ParamNameActionResolver which in turn returns a
 * ParamValueActionResolver and so on alternatingly.
 */
public class ParameterisedActionResolver extends ActionResolverSupport
{
    public ParameterisedActionResolver(String action)
    {
        super(action);
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
