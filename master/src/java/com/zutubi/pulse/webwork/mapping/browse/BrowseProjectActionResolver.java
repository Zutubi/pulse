package com.zutubi.pulse.webwork.mapping.browse;

import com.zutubi.pulse.webwork.mapping.PagedActionResolver;
import com.zutubi.pulse.webwork.mapping.ParameterisedActionResolver;
import com.zutubi.pulse.webwork.mapping.StaticMapActionResolver;

/**
 */
public class BrowseProjectActionResolver extends StaticMapActionResolver
{
    public BrowseProjectActionResolver(String project)
    {
        super("currentBuild");

        addMapping("home", new ParameterisedActionResolver("currentBuild"));
        addMapping("reports", new ParameterisedActionResolver("projectReports"));
        addMapping("history", new PagedActionResolver("history"));
        addMapping("builds", new ProjectBuildsActionResolver());
        addMapping("changes", new ChangelistsActionResolver());

        addParameter("projectName", project);
    }
}
