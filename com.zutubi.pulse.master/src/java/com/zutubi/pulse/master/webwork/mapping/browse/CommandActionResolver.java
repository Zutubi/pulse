package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ActionResolverSupport;

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
