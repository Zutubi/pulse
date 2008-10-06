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
