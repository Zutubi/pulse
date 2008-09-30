package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.ParameterisedActionResolver;

/**
 */
public class ProjectActionActionResolver extends ParameterisedActionResolver
{
    public ProjectActionActionResolver(String action)
    {
        super("projectAction");
        addParameter("action", action);
    }
}
