package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ActionResolver;

import java.util.Collections;
import java.util.Map;

/**
 */
public class ProjectBuildsActionResolver implements ActionResolver
{
    public String getAction()
    {
        return null;
    }

    public Map<String, String> getParameters()
    {
        return Collections.EMPTY_MAP;
    }

    public ActionResolver getChild(String name)
    {
        return new BuildActionResolver(name);
    }
}
