package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.ParameterisedActionResolver;

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
