package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.config.Resource;

import java.io.File;
import java.io.IOException;

/**
 * <class comment/>
 */
public class Maven2ResourceConstructor implements ResourceConstructor
{
    private StandardTemplateResourceConstructor constructor;

    public Maven2ResourceConstructor()
    {
        constructor = new StandardTemplateResourceConstructor();
        constructor.setResourceName("maven2");
        constructor.setScriptName("mvn");
    }

    public boolean isResourceHome(String path)
    {
        return constructor.isResourceHome(path);
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
        return System.getenv("MAVEN2_HOME");
    }
}
