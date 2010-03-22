package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;

import java.util.Arrays;
import java.util.List;

/**
 * Resolves down to the collection of stage logs, requires a stage name to be
 * specified.
 */
public class StageLogsActionResolver extends ActionResolverSupport
{
    public StageLogsActionResolver()
    {
        super(null);
    }

    @Override
    public List<String> listChildren()
    {
        return Arrays.asList("<stage>");
    }

    @Override
    public ActionResolver getChild(String name)
    {
        return new StageLogActionResolver(name);
    }
}
