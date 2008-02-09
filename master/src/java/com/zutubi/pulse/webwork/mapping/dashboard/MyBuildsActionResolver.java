package com.zutubi.pulse.webwork.mapping.dashboard;

import com.zutubi.pulse.webwork.mapping.ActionResolver;
import com.zutubi.pulse.webwork.mapping.browse.BuildActionResolver;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class MyBuildsActionResolver implements ActionResolver
{
    private static final Map<String, String> PARAMETER_MAP = new HashMap<String, String>(1);
    static
    {
        PARAMETER_MAP.put("personal", "true");
    }
    
    public String getAction()
    {
        return "my";
    }

    public Map<String, String> getParameters()
    {
        return PARAMETER_MAP;
    }

    public ActionResolver getChild(String name)
    {
        return new BuildActionResolver(name);
    }
}
