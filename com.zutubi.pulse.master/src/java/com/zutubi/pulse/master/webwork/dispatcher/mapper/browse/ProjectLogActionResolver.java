package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;

/**
 * Action resolver for the project log.
 */
public class ProjectLogActionResolver  extends ParameterisedActionResolver
{
    public ProjectLogActionResolver()
    {
        super("tailProjectLog");
    }
}
