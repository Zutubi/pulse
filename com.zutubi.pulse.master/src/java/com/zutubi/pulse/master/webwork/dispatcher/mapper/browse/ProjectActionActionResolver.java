package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;

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
