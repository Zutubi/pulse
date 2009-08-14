package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.PagedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ParameterisedActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.StaticMapActionResolver;

/**
 */
public class BrowseProjectActionResolver extends StaticMapActionResolver
{
    public BrowseProjectActionResolver(String project)
    {
        super("projectHome");

        addMapping("home", new ParameterisedActionResolver("projectHome"));
        addMapping("reports", new ProjectReportsActionResolver());
        addMapping("history", new PagedActionResolver("history"));
        addMapping("dependencies", new ParameterisedActionResolver("projectDependencies"));
        addMapping("log", new ProjectLogActionResolver());
        addMapping("builds", new ProjectBuildsActionResolver());
        addMapping("changes", new ChangelistsActionResolver());
        addMapping("actions", new ProjectActionsActionResolver());

        addParameter("projectName", project);
    }
}
