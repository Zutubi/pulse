package com.zutubi.pulse.master.webwork.mapping;

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

    public ActionResolver getChild(String name)
    {
        return new ParamNameActionResolver(getAction(), name);
    }
}
