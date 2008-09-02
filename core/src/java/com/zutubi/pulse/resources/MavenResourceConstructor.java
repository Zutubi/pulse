package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.config.Resource;

import java.io.File;
import java.io.IOException;

/**
 * <class comment/>
 */
public class MavenResourceConstructor implements ResourceConstructor
{
    private StandardTemplateResourceConstructor constructor;

    public MavenResourceConstructor()
    {
        constructor = new StandardTemplateResourceConstructor();
        constructor.setResourceName("maven");
        constructor.setScriptName("maven");
    }

    public boolean isResourceHome(String home)
    {
        return constructor.isResourceHome(home);
    }

    public boolean isResourceHome(File home)
    {
        return constructor.isResourceHome(home);
    }

    public Resource createResource(String home) throws IOException
    {
        return constructor.createResource(home);
    }

    public Resource createResource(File home) throws IOException
    {
        return constructor.createResource(home);
    }

    public String lookupHome()
    {
        return System.getenv("MAVEN_HOME");
    }
}
