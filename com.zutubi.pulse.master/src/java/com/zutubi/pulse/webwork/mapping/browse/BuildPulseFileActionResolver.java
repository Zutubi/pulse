package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ParameterisedActionResolver;
import com.zutubi.pulse.webwork.mapping.StaticMapActionResolver;

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
