package com.zutubi.pulse.webwork.mapping;

/**
 */
public class CommandActionResolver extends ActionResolverSupport
{
    public CommandActionResolver(String action, String command)
    {
        super(action);
        addParameter("command", command);
    }
}
