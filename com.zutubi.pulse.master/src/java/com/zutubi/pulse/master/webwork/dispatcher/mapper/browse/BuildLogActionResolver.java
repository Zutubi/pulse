package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;

/**
 * Resolves to a view of the build log.
 */
public class BuildLogActionResolver extends ParameterisedActionResolver
{
    public BuildLogActionResolver()
    {
        super("tailBuildLog");
        addParameter("buildSelected", "true");
    }
}
