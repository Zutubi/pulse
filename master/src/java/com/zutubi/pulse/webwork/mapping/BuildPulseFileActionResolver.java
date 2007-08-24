package com.zutubi.pulse.webwork.mapping;

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
