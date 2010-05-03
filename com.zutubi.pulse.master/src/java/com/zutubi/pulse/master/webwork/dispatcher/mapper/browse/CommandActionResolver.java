package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

/**
 */
public class CommandActionResolver extends ActionResolverSupport
{
    public CommandActionResolver(String action, String command)
    {
        super(action);
        addParameter("commandName", command);
    }
}
