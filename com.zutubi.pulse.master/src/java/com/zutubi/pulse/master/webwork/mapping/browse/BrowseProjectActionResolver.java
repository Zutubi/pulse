package com.zutubi.pulse.master.webwork.mapping.browse;

import com.zutubi.pulse.master.webwork.mapping.PagedActionResolver;
import com.zutubi.pulse.master.webwork.mapping.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.mapping.StaticMapActionResolver;

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
        addMapping("actions", new ProjectActionsActionResolver());

        addParameter("projectName", project);
    }
}
