package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;

import java.util.Arrays;
import java.util.List;

/**
 * Resolves to the projectReports action, capturing the group name if
 * provided.
 */
public class ProjectReportsActionResolver extends ActionResolverSupport
{
    private static final String ACTION = "projectReports";

    public ProjectReportsActionResolver()
    {
        super(ACTION);
    }

    @Override
    public List<String> listChildren()
    {
        return Arrays.asList("<group>");
    }

    @Override
    public ActionResolver getChild(String name)
    {
        ParameterisedActionResolver child = new ParameterisedActionResolver(ACTION);
        child.addParameter("group", name);
        return child;
    }
}
