package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.mapping.StaticMapActionResolver;

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
