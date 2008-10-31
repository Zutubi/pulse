package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 */
public class BuildPulseFileActionResolver extends StaticMapActionResolver
{
    public BuildPulseFileActionResolver()
    {
        super("viewBuildFile");
        addMapping("raw", new ParameterisedActionResolver("downloadBuildFile"));
    }
}
