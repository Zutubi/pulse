package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.model.Resource;

import java.io.File;
import java.io.IOException;

/**
 * <class comment/>
 */
public class AntResourceConstructor implements ResourceConstructor
{
    private StandardTemplateResourceConstructor constructor;

    public AntResourceConstructor()
    {
        constructor = new StandardTemplateResourceConstructor();
        constructor.setResourceName("ant");
        constructor.setScriptName("ant");
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
}
