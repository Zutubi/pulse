package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 */
public class StageActionResolver extends ActionResolverSupport
{
    public StageActionResolver(String action, String stage)
    {
        super(action);
        addParameter("stageName", stage);
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<command>");
    }

    public ActionResolver getChild(String name)
    {
        return new CommandActionResolver(getAction(), name);
    }
}
