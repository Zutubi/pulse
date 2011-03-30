package com.zutubi.pulse.master.webwork.dispatcher.mapper.server;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.PagedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 * Resolver for the root of the server/ namespace.
 */
public class ServerActionResolver extends StaticMapActionResolver
{
    public ServerActionResolver()
    {
        super("viewServerQueues");
        addMapping("activity", new ParameterisedActionResolver("viewServerQueues"));
        addMapping("history", new PagedActionResolver("serverHistory"));
        addMapping("messages", new PagedActionResolver("serverMessages"));
        addMapping("info", new ParameterisedActionResolver("viewSystemInfo"));
    }
}
