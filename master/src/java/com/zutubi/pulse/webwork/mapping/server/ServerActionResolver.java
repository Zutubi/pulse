package com.zutubi.pulse.webwork.mapping.server;

import com.zutubi.pulse.webwork.mapping.PagedActionResolver;
import com.zutubi.pulse.webwork.mapping.ParameterisedActionResolver;
import com.zutubi.pulse.webwork.mapping.StaticMapActionResolver;

/**
 * Resolver for the root of the server/ namespace.
 */
public class ServerActionResolver extends StaticMapActionResolver
{
    public ServerActionResolver()
    {
        super("viewServerQueues");
        addMapping("activity", new ParameterisedActionResolver("viewServerQueues"));
        addMapping("messages", new PagedActionResolver("serverMessages"));
        addMapping("info", new ParameterisedActionResolver("viewSystemInfo"));
    }
}
